package org.aya.intellij.lsp;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import kala.collection.Seq;
import kala.collection.SeqView;
import kala.collection.immutable.ImmutableMap;
import kala.collection.immutable.ImmutableSeq;
import kala.collection.mutable.MutableMap;
import kala.collection.mutable.MutableSet;
import kala.function.CheckedConsumer;
import kala.function.CheckedFunction;
import kala.function.CheckedSupplier;
import org.aya.cli.library.incremental.InMemoryCompilerAdvisor;
import org.aya.cli.library.source.LibrarySource;
import org.aya.generic.Constants;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.aya.intellij.psi.ref.AyaPsiReference;
import org.aya.lsp.actions.GotoDefinition;
import org.aya.lsp.server.AyaLanguageClient;
import org.aya.lsp.server.AyaServer;
import org.aya.lsp.server.AyaService;
import org.aya.lsp.utils.Log;
import org.aya.lsp.utils.Resolver;
import org.aya.ref.Var;
import org.aya.util.distill.DistillerOptions;
import org.aya.util.error.SourcePos;
import org.aya.util.error.WithPos;
import org.aya.util.reporter.Problem;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bridges between the Aya LSP and the IntelliJ platform.
 * To decouple as much as possible, only the most important features (i.e. {@link AyaPsiReference})
 * are implemented by directly calling the LSP.
 * Other features are implemented by using Intellij platform APIs. See {@link org.aya.intellij.actions.SemanticHighlight}
 * for example, which makes use of {@link AyaPsiReference#resolve()}
 * instead of querying the LSP for highlight results.
 */
public final class AyaLsp extends InMemoryCompilerAdvisor implements AyaLanguageClient {
  private static final @NotNull Key<AyaLsp> AYA_LSP = Key.create("intellij.aya.lsp");
  private static final @NotNull Logger LOG = Logger.getInstance(AyaLsp.class);
  private final @NotNull AyaServer server;
  private final @NotNull AyaService service;
  private final @NotNull MutableSet<VirtualFile> libraryPathCache = MutableSet.create();
  private final @NotNull MutableMap<Path, ImmutableSeq<Problem>> problemCache = MutableMap.create();
  private final @NotNull ExecutorService compilerPool = Executors.newFixedThreadPool(1);

  public static void start(@NotNull VirtualFile ayaJson, @NotNull Project project) {
    Log.i("[intellij-aya] Hello, this is Aya Language Server inside intellij-aya.");
    var lsp = new AyaLsp();
    lsp.registerLibrary(ayaJson.getParent());
    lsp.recompile(null);
    project.putUserData(AYA_LSP, lsp);
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override public void after(@NotNull List<? extends VFileEvent> events) {
        lsp.fireVfsEvent(events);
      }
    });
  }

  /**
   * A fallback behavior when LSP is not available is required.
   * Use {@link #use(Project, CheckedConsumer)} or {@link #use(Project, CheckedFunction, CheckedSupplier)} instead.
   */
  private static @Nullable AyaLsp of(@NotNull Project project) {
    return project.getUserData(AYA_LSP);
  }

  private static boolean isAvailable(@NotNull Project project) {
    return of(project) != null;
  }

  public static <E extends Throwable> void use(
    @NotNull Project project,
    @NotNull CheckedConsumer<AyaLsp, E> block
  ) throws E {
    var lsp = of(project);
    if (lsp != null) block.acceptChecked(lsp);
  }

  public static <R, E extends Throwable> R use(
    @NotNull Project project,
    @NotNull CheckedFunction<AyaLsp, R, E> block,
    @NotNull CheckedSupplier<R, E> orElse
  ) throws E {
    var lsp = of(project);
    if (lsp == null) return orElse.getChecked();
    return block.applyChecked(lsp);
  }

  public AyaLsp() {
    this.server = new AyaServer(this);
    this.service = server.getTextDocumentService();
    server.connect(this);
  }

  void fireVfsEvent(List<? extends VFileEvent> events) {
    var any = Seq.wrapJava(events).view()
      .filterIsInstance(VFileContentChangeEvent.class)
      .map(VFileContentChangeEvent::getFile)
      .anyMatch(this::isSourceChanged);
    if (any) recompile(() -> {
      Arrays.stream(ProjectManager.getInstance().getOpenProjects())
        .filter(AyaLsp::isAvailable)
        .forEach(p -> DaemonCodeAnalyzer.getInstance(p).restart());
      Log.i("[intellij-aya] Restarted DaemonCodeAnalyzer");
    });
  }

  boolean isSourceChanged(@NotNull VirtualFile file) {
    return isInLibrary(file) && file.getName().endsWith(Constants.AYA_POSTFIX);
  }

  void recompile(@Nullable Runnable callback) {
    recompile(() -> service.libraries().forEach(service::loadLibrary), callback);
  }

  void recompile(@NotNull Runnable compile, @Nullable Runnable callback) {
    compilerPool.execute(() -> {
      Log.i("[intellij-aya] Compilation started.");
      compile.run();
      if (callback != null) callback.run();
    });
  }

  public void registerLibrary(@NotNull VirtualFile library) {
    if (JB.fileSupported(library)) {
      libraryPathCache.add(library);
      service.registerLibrary(JB.canonicalize(library));
    }
  }

  public boolean isInLibrary(@Nullable VirtualFile file) {
    while (file != null && file.isValid() && JB.fileSupported(file)) {
      if (libraryPathCache.contains(file)) return true;
      file = file.getParent();
    }
    return false;
  }

  public @Nullable LibrarySource sourceFileOf(@NotNull AyaPsiElement element) {
    var vf = element.getContainingFile().getVirtualFile();
    return JB.fileSupported(vf) ? service.find(JB.canonicalize(vf)) : null;
  }

  /**
   * Jump to the defining {@link Var} from the psi element position.
   *
   * @return The psi element that defined the var.
   */
  public @NotNull SeqView<PsiElement> gotoDefinition(@NotNull AyaPsiElement element) {
    var proj = element.getProject();
    var source = sourceFileOf(element);
    return source == null
      ? SeqView.empty()
      : GotoDefinition.findDefs(source, JB.toXyPosition(element), service.libraries())
      .map(WithPos::data)
      .mapNotNull(pos -> elementAt(proj, pos));
  }

  /** Get the {@link Var} defined by the psi element. */
  public @NotNull SeqView<WithPos<Var>> resolveVarDefinedBy(@NotNull AyaPsiNamedElement element) {
    var source = sourceFileOf(element);
    if (source == null) return SeqView.empty();
    return Resolver.resolveVar(source, JB.toXyPosition(element));
  }

  private @NotNull SeqView<Problem> problemsFor(@NotNull PsiFile element) {
    if (problemCache.isEmpty()) return SeqView.empty();
    var vf = element.getVirtualFile();
    if (vf == null || !JB.fileSupported(vf)) return SeqView.empty();
    var path = vf.toNioPath();
    var problems = problemCache.getOrNull(path);
    return problems != null ? problems.view() : SeqView.empty();
  }

  public @NotNull SeqView<Problem> errorsInFile(@NotNull PsiFile file) {
    return problemsFor(file).filter(Problem::isError);
  }

  public @NotNull <T extends Problem> SeqView<T> warningsInFile(@NotNull PsiFile file, @NotNull Class<T> type) {
    return problemsFor(file)
      .filter(p -> p.level() == Problem.Severity.WARN)
      .filterIsInstance(type);
  }

  public @NotNull <T extends Problem> SeqView<T> warningsAt(@NotNull PsiElement element, @NotNull Class<T> type) {
    return warningsInFile(element.getContainingFile(), type)
      .filter(p -> JB.toRange(p.sourcePos()).containsOffset(element.getTextOffset()));
  }

  private @Nullable PsiElement elementAt(@NotNull Project project, @NotNull SourcePos pos) {
    return pos.file().underlying()
      .mapNotNull(path -> VirtualFileManager.getInstance().findFileByNioPath(path))
      .mapNotNull(virtualFile -> PsiManager.getInstance(project).findFile(virtualFile))
      .mapNotNull(psiFile -> psiFile.findElementAt(JB.toRange(pos).getStartOffset()))
      .getOrNull();
  }

  @Override public void publishAyaProblems(
    @NotNull ImmutableMap<Path, ImmutableSeq<Problem>> problems,
    @NotNull DistillerOptions options
  ) {
    problemCache.putAll(problems);
  }

  @Override public void clearAyaProblems(@NotNull ImmutableSeq<Path> files) {
    files.forEach(problemCache::remove);
  }

  @Override public void logMessage(@NotNull MessageParams message) {
    switch (message.getType()) {
      case Error -> LOG.error(message.getMessage());
      case Warning -> LOG.warn(message.getMessage());
      case Info -> LOG.info(message.getMessage());
      case Log -> LOG.debug(message.getMessage());
    }
  }

  @Override public void showMessage(MessageParams messageParams) {
    throw new IllegalStateException("unreachable");
  }

  @Override public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
    throw new IllegalStateException("unreachable");
  }

  @Override public void telemetryEvent(Object object) {
    throw new IllegalStateException("unreachable");
  }

  @Override public void publishDiagnostics(@NotNull PublishDiagnosticsParams diagnostics) {
    throw new IllegalStateException("unreachable");
  }
}
