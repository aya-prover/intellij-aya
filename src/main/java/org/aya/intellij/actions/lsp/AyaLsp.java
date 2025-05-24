package org.aya.intellij.actions.lsp;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileUtil;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableMap;
import kala.collection.immutable.ImmutableSeq;
import kala.collection.mutable.MutableList;
import kala.collection.mutable.MutableMap;
import kala.collection.mutable.MutableSet;
import kala.function.CheckedConsumer;
import kala.function.CheckedFunction;
import kala.function.CheckedSupplier;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.ThreadPoolDispatcherKt;
import org.aya.cli.library.incremental.InMemoryCompilerAdvisor;
import org.aya.cli.library.source.LibraryOwner;
import org.aya.cli.library.source.LibrarySource;
import org.aya.generic.Constants;
import org.aya.ide.Resolver;
import org.aya.ide.action.GotoDefinition;
import org.aya.intellij.AyaBundle;
import org.aya.intellij.actions.completion.CompletionsKt;
import org.aya.intellij.language.AyaIJParserImpl;
import org.aya.intellij.notification.AyaNotification;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiFile;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.aya.intellij.psi.AyaPsiReference;
import org.aya.intellij.service.DistillerService;
import org.aya.intellij.service.ProblemService;
import org.aya.lsp.actions.CompletionProvider;
import org.aya.lsp.models.ProjectPath;
import org.aya.lsp.server.AyaLanguageClient;
import org.aya.lsp.server.AyaLanguageServer;
import org.aya.lsp.utils.Log;
import org.aya.resolve.context.Candidate;
import org.aya.syntax.GenericAyaParser;
import org.aya.syntax.ref.AnyVar;
import org.aya.syntax.ref.DefVar;
import org.aya.tyck.error.Goal;
import org.aya.util.PrettierOptions;
import org.aya.util.position.WithPos;
import org.aya.util.reporter.Problem;
import org.aya.util.reporter.Reporter;
import org.intellij.lang.annotations.MagicConstant;
import org.javacs.lsp.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Bridges between the Aya LSP and the IntelliJ platform.
 * To decouple as much as possible, only the most important features (i.e. {@link AyaPsiReference})
 * are implemented by directly calling the LSP.
 * Other features are implemented by using Intellij platform APIs. See {@link org.aya.intellij.actions.SemanticHighlight}
 * for example, which makes use of {@link AyaPsiReference#resolve()}
 * instead of querying the LSP for highlight results.
 */
public final class AyaLsp extends InMemoryCompilerAdvisor implements AyaLanguageClient, CoroutineScope {
  private static final @NotNull Key<AyaLsp> AYA_LSP = Key.create("intellij.aya.lsp");
  private static final @NotNull Logger LOG = Logger.getInstance(AyaLsp.class);
  private final @NotNull AyaLanguageServer server;
  private final @NotNull Project project;
  private final @NotNull MutableSet<VirtualFile> librarySrcPathCache = MutableSet.create();
  private final @NotNull MutableMap<Path, ImmutableSeq<Problem>> problemCache = MutableMap.create();
  /// TODO: reuse [AyaLsp#dispatcher]?
  private final @NotNull ExecutorService compilerPool = Executors.newFixedThreadPool(1);

  public static @NotNull AyaLsp start(@NotNull Project project, @NotNull VirtualFile projectOrFile) {
    var lsp = start(project);
    lsp.registerLibrary(projectOrFile);
    return lsp;
  }

  /// Start [AyaLsp] for {@param project}, note that this method is NOT thread-safe, see {@link AyaStartupKt#startLsp}
  public static @NotNull AyaLsp start(@NotNull Project project) {
    Log.i("[intellij-aya] Hello, this is Aya Language Server inside intellij-aya.");
    var lsp = new AyaLsp(project);
    lsp.server.initialize(new InitializeParams());
    project.putUserData(AYA_LSP, lsp);
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        lsp.fireVfsEvent(true, ImmutableSeq.from(events));
      }

      @Override public void after(@NotNull List<? extends VFileEvent> events) {
        lsp.fireVfsEvent(false, ImmutableSeq.from(events));
      }
    });

    return lsp;
  }

  /**
   * A fallback behavior when LSP is not available is required.
   * Use {@link #use(Project, CheckedSupplier, CheckedFunction)} instead.
   */
  static @Nullable AyaLsp of(@NotNull Project project) {
    return project.getUserData(AYA_LSP);
  }

  public static boolean isActive(@NotNull Project project) {
    return of(project) != null;
  }

  public static void useUnchecked(
    @NotNull Project project,
    @NotNull Consumer<AyaLsp> block
  ) {
    var lsp = of(project);
    if (lsp != null) block.accept(lsp);
  }

  public static <R> R useUnchecked(
    @NotNull Project project,
    @NotNull Supplier<R> orElse,
    @NotNull Function<AyaLsp, R> block
  ) {
    var lsp = of(project);
    if (lsp == null) return orElse.get();
    return block.apply(lsp);
  }

  public static <R, E extends Throwable> R use(
    @NotNull Project project,
    @NotNull CheckedSupplier<R, E> orElse,
    @NotNull CheckedFunction<AyaLsp, R, E> block
  ) throws E {
    var lsp = of(project);
    if (lsp == null) return orElse.getChecked();
    return block.applyChecked(lsp);
  }

  public static <E extends Throwable> void use(
    @NotNull Project project,
    @NotNull CheckedConsumer<AyaLsp, E> block
  ) throws E {
    var lsp = of(project);
    if (lsp != null) block.acceptChecked(lsp);
  }

  public AyaLsp(@NotNull Project project) {
    this.project = project;
    this.server = new AyaLanguageServer(this, this);
  }

  void fireVfsEvent(boolean before, @NotNull ImmutableSeq<? extends VFileEvent> events) {
    var lspEvents = events.view()
      .flatMap(e -> processVfsEvent(before, e))
      .toSeq();
    Log.d("[intellij-aya] =================== FILE EVENTS ====================");
    if (lspEvents.isNotEmpty()) {
      Log.d("[intellij-aya] Sending these file change events to LSP:");
      lspEvents.forEach(e -> Log.d("[intellij-aya]   - %s", e));
      var params = new DidChangeWatchedFilesParams();
      params.changes = lspEvents.map(VfsAction::lspFileEvent).asJava();
      server.didChangeWatchedFiles(params);
    }
    if (lspEvents.anyMatch(VfsAction::shouldRecompile)) {
      Log.d("[intellij-aya] A bunch of files have been changed, recompiling");
      recompile(() -> {
        DaemonCodeAnalyzer.getInstance(project).restart();
        Log.d("[intellij-aya] Restarted DaemonCodeAnalyzer");
      });
    }
    Log.d("[intellij-aya] =================== FILE EVENTS ====================");
  }

  record VfsAction(boolean shouldRecompile, @NotNull FileEvent lspFileEvent) {
    @Override public String toString() {
      return "(%s, %s) '%s'".formatted(switch (lspFileEvent.type) {
        case FileChangeType.Created -> "Created";
        case FileChangeType.Changed -> "Modified";
        case FileChangeType.Deleted -> "Deleted";
        default -> "Unknown";
      }, shouldRecompile ? "+" : "-", lspFileEvent.uri);
    }
  }

  private @NotNull FileEvent createLspFileEvent(@NotNull VirtualFile file, @MagicConstant(valuesFromClass = FileChangeType.class) int type) {
    var lspEvent = new FileEvent();
    lspEvent.type = type;
    lspEvent.uri = JB.canonicalizedUri(file);
    return lspEvent;
  }

  @NotNull ImmutableSeq<@NotNull VfsAction> fileCreatedEvent(boolean shouldRecompile, @Nullable VirtualFile file) {
    if (file == null || ! file.isValid() || file.isDirectory() || !isWatched(file)) return ImmutableSeq.empty();
    return ImmutableSeq.of(new VfsAction(shouldRecompile, createLspFileEvent(file, FileChangeType.Created)));
  }

  @NotNull ImmutableSeq<@NotNull VfsAction> fileDeletedEvent(boolean shouldRecompile, @Nullable VirtualFile file) {
    if (file == null) return ImmutableSeq.empty();
    return file.isDirectory()
      ? ImmutableSeq.of(file.getChildren()).flatMap(c -> fileDeletedEvent(shouldRecompile, c))
      : isWatched(file)
      ? ImmutableSeq.of(new VfsAction(shouldRecompile, createLspFileEvent(file, FileChangeType.Deleted)))
      : ImmutableSeq.empty();
  }

  @NotNull ImmutableSeq<@NotNull VfsAction> fileModifiedEvent(boolean shouldRecompile, @Nullable VirtualFile file) {
    if (file == null || file.isDirectory() || !isWatched(file)) return ImmutableSeq.empty();
    return ImmutableSeq.of(new VfsAction(shouldRecompile, createLspFileEvent(file, FileChangeType.Changed)));
  }

  @NotNull ImmutableSeq<@NotNull VfsAction> processVfsEvent(boolean before, @Nullable VFileEvent event) {
    Log.d("[intellij-aya] (%s) VFS event: %s", before ? "Before" : "After", event);
    var after = !before;
    return switch (event) {
      case VFileContentChangeEvent e when after -> fileModifiedEvent(true, e.getFile());
      case VFileCreateEvent e when after -> fileCreatedEvent(true, e.getFile());
      case VFileDeleteEvent e when before -> fileDeletedEvent(true, e.getFile());
      case VFileCopyEvent e when after -> fileCreatedEvent(true, e.findCreatedFile());
      // do not trigger recompilation before moving files, do it after the move.
      case VFileMoveEvent e when before -> fileDeletedEvent(false, e.getFile());
      case VFileMoveEvent e /*when after*/ -> fileCreatedEvent(true, e.getFile());
      case null, default -> ImmutableSeq.empty();
    };
  }

  public boolean isWatched(@Nullable VirtualFile file) {
    return isInLibrary(file) && file.getName().endsWith(Constants.AYA_POSTFIX);
  }

  void recompile(@Nullable Runnable callback) {
    recompile(server::reload, callback);
  }

  void recompile(@NotNull Runnable compile, @Nullable Runnable callback) {
    compilerPool.execute(() -> {
      Log.d("[intellij-aya] =================== COMPILATION ====================");
      Log.i("[intellij-aya] Compilation started.");
      var service = project.getService(ProblemService.class);
      compile.run();
      service.allProblems.set(ImmutableMap.from(problemCache));
      Log.i("[intellij-aya] Compilation finished.");
      if (callback != null) callback.run();
      Log.i("[intellij-aya] Compilation finishing notified.");
      Log.d("[intellij-aya] =================== COMPILATION ====================");
    });
  }

  public boolean isLibraryLoaded(@NotNull VirtualFile projectOrFile) {
    return getLoadedLibrary(projectOrFile) != null;
  }

  public @Nullable LibraryOwner getLoadedLibrary(@NotNull VirtualFile projectOrFile) {
    if (JB.fileSupported(projectOrFile)) {
      var path = ProjectPath.resolve(projectOrFile.toNioPath());
      if (path == null) return null;
      return server.getRegisteredLibrary(path);
    }

    return null;
  }

  /// Register an aya project or a single aya file to lsp
  ///
  /// @param projectOrFile a aya project directory, "aya.json" file, or a single aya file
  public void registerLibrary(@NotNull VirtualFile projectOrFile) {
    if (JB.fileSupported(projectOrFile)) {
      var root = JB.canonicalize(projectOrFile);
      var paths = server.registerLibrary(root).flatMap(registeredLibrary ->
        registeredLibrary.modulePath()
          .mapNotNull(path -> projectOrFile.findFileByRelativePath(root.relativize(path).toString())));
      librarySrcPathCache.addAll(paths);
      recompile(null);
    }
  }

  boolean isInLibrary(@Nullable VirtualFile file) {
    while (file != null && file.isValid() && JB.fileSupported(file)) {
      if (librarySrcPathCache.contains(file)) return true;
      file = file.getParent();
    }
    return false;
  }

  public @Nullable LibrarySource sourceFileOf(@NotNull AyaPsiElement element) {
    var vf = element.getContainingFile().getVirtualFile();
    // may exists in memory, try originalFile
    if (vf == null) {
      vf = VirtualFileUtil.originalFile(element.getContainingFile().getViewProvider().getVirtualFile());
    }

    if (vf == null) return null;
    return JB.fileSupported(vf) ? server.find(JB.canonicalize(vf)) : null;
  }

  /// region LSP Actions

  /**
   * Jump to the defining {@link AnyVar} from the psi element position.
   *
   * @return The psi element that defined the var.
   */
  public @NotNull SeqView<AyaPsiNamedElement> gotoDefinition(@NotNull AyaPsiElement element) {
    var proj = element.getProject();
    var source = sourceFileOf(element);
    return source == null ? SeqView.empty() : GotoDefinition.findDefs(source, server.libraries(), JB.toXY(element))
      .map(WithPos::data)
      .mapNotNull(pos -> JB.elementAt(proj, pos, AyaPsiNamedElement.class));
  }

  /** Get the {@link AnyVar} defined by the psi element. */
  public @NotNull SeqView<WithPos<AnyVar>> resolveVarDefinedBy(@NotNull AyaPsiNamedElement element) {
    var source = sourceFileOf(element);
    if (source == null) return SeqView.empty();
    return Resolver.resolveVar(source, JB.toXY(element));
  }

  private @NotNull SeqView<Problem> problemsFor(@NotNull PsiFile file) {
    if (problemCache.isEmpty()) return SeqView.empty();
    var vf = file.getVirtualFile();
    if (vf == null || !JB.fileSupported(vf)) return SeqView.empty();
    var path = JB.canonicalize(vf);
    var problems = problemCache.getOrNull(path);
    var maxOffset = file.getTextLength();
    return problems == null ? SeqView.empty() : problems.view()
      .filter(p -> JB.endOffset(p.sourcePos()) <= maxOffset);
  }

  public @NotNull SeqView<Problem> errorsInFile(@NotNull PsiFile file) {
    return problemsFor(file).filter(Problem::isError);
  }

  public @NotNull SeqView<Problem> warningsInFile(@NotNull PsiFile file) {
    return problemsFor(file)
      .filter(p -> p.level() == Problem.Severity.WARN);
  }

  public @NotNull <T extends Problem> SeqView<T> warningsInFile(@NotNull PsiFile file, @NotNull Class<T> type) {
    return warningsInFile(file).filterIsInstance(type);
  }

  public @NotNull SeqView<Goal> goalsInFile(@NotNull PsiFile file) {
    // note: aya only has one goal problem type.
    return problemsFor(file)
      .filter(p -> p.level() == Problem.Severity.GOAL)
      .filterIsInstance(Goal.class);
  }

  public @NotNull SeqView<Problem> infosInFile(@NotNull PsiFile file) {
    return problemsFor(file)
      .filter(p -> p.level() == Problem.Severity.INFO);
  }

  public @NotNull <T extends Problem> SeqView<T> warningsAt(@NotNull PsiElement element, @NotNull Class<T> type) {
    return warningsInFile(element.getContainingFile(), type)
      .filter(p -> JB.toRange(p.sourcePos()).containsOffset(element.getTextOffset()));
  }

  public @NotNull SeqView<Goal> goalsAt(@NotNull PsiElement element) {
    return goalsAt(element.getContainingFile(), element.getTextOffset());
  }

  public @NotNull SeqView<Goal> goalsAt(@NotNull PsiFile file, int textOffset) {
    var goals = goalsInFile(file);
    return goals.filter(p -> JB.toRange(p.sourcePos()).containsOffset(textOffset));
  }

  public @NotNull SeqView<DefVar<?, ?>> symbolsInFile(@NotNull AyaPsiFile file) {
    var source = sourceFileOf(file);
    if (source == null) return SeqView.empty();
    var list = MutableList.<DefVar<?, ?>>create();
    source.resolveInfo().get().thisModule().symbols()
      .view()
      .valuesView()
      .flatMap(Candidate::getAll)
      .mapNotNullTo(list, c -> c instanceof DefVar<?,?> d ? d : null);

    return list.view();
  }

  public @NotNull LookupElement[] collectCompletionItem(@NotNull AyaPsiElement element) {
    var file = sourceFileOf(element);
    if (file == null) return LookupElement.EMPTY_ARRAY;

    var xy = JB.toXY(element);
    var result = CompletionProvider.completion(file, xy, doc -> doc.easyToString());
    return CompletionsKt.toLookupElements(result);
  }

  /// endregion LSP Actions

  @Override public void publishAyaProblems(
    @NotNull ImmutableMap<Path, ImmutableSeq<Problem>> problems,
    @NotNull PrettierOptions options
  ) {
    problemCache.putAll(problems);
    problems.forEach((f, ps) -> {
      Log.d("[intellij-aya] Problems in %s", f.toAbsolutePath().toString());
      ps.forEach(p -> Log.d("[intellij-aya]   - %s", DistillerService.plainBrief(p)));
    });
  }

  @Override public void clearAyaProblems(@NotNull ImmutableSeq<Path> files) {
    files.forEach(problemCache::remove);
  }

  @Override public void logMessage(@NotNull ShowMessageParams message) {
    switch (message.type) {
      case MessageType.Error -> LOG.error(message.message);
      case MessageType.Warning -> LOG.warn(message.message);
      case MessageType.Info -> LOG.info(message.message);
      case MessageType.Log -> LOG.debug(message.message);
    }
  }

  @Override
  public void showMessage(@NotNull ShowMessageParams params) {
    var type = switch (params.type) {
      case MessageType.Error -> NotificationType.ERROR;
      case MessageType.Warning -> NotificationType.WARNING;
      case MessageType.Info, MessageType.Log -> NotificationType.INFORMATION;
      default -> null;
    };

    var notifier = AyaNotification.BALLOON;
    var title = AyaBundle.INSTANCE.message("aya.notification.lsp.title");
    var content = type != null
      ? params.message
      : AyaBundle.INSTANCE.message("aya.notification.lsp.bad.message.type.content", params.type);

    notifier.createNotification(content, NotificationType.ERROR)
      .setTitle(title)
      .notify(project);
  }

  @Override public @NotNull GenericAyaParser createParser(@NotNull Reporter reporter) {
    return new AyaIJParserImpl(project, reporter);
  }

  // region Coroutine

  private final @NotNull CoroutineDispatcher dispatcher = ThreadPoolDispatcherKt.newSingleThreadContext(Objects.toIdentityString(this));

  @Override
  public @NotNull CoroutineContext getCoroutineContext() {
    return dispatcher;
  }

  // endreigon Coroutine
}
