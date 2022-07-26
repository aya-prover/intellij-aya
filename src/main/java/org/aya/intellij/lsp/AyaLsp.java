package org.aya.intellij.lsp;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import kala.collection.Seq;
import kala.collection.SeqLike;
import kala.collection.SeqView;
import kala.collection.mutable.MutableMap;
import kala.collection.mutable.MutableSet;
import kala.control.Option;
import kala.function.CheckedConsumer;
import kala.function.CheckedFunction;
import kala.function.CheckedSupplier;
import org.aya.cli.library.source.LibrarySource;
import org.aya.generic.Constants;
import org.aya.intellij.psi.AyaPsiElement;
import org.aya.intellij.psi.AyaPsiNamedElement;
import org.aya.lsp.actions.GotoDefinition;
import org.aya.lsp.models.HighlightResult;
import org.aya.lsp.server.AyaLanguageClient;
import org.aya.lsp.server.AyaServer;
import org.aya.lsp.server.AyaService;
import org.aya.lsp.utils.Log;
import org.aya.lsp.utils.Resolver;
import org.aya.ref.Var;
import org.aya.util.error.SourcePos;
import org.aya.util.error.WithPos;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class AyaLsp implements AyaLanguageClient {
  private static final @NotNull Key<AyaLsp> AYA_LSP = Key.create("intellij.aya.lsp");
  private static final @NotNull Logger LOG = Logger.getInstance(AyaLsp.class);
  private final @NotNull AyaServer server;
  private final @NotNull AyaService service;
  private final @NotNull MutableMap<Path, MutableMap<TextRange, HighlightResult.Kind>> highlightCache = MutableMap.create();
  private final @NotNull MutableSet<VirtualFile> libraryPathCache = MutableSet.create();
  private final @NotNull ExecutorService compilerPool = Executors.newFixedThreadPool(1);

  public static void start(@NotNull VirtualFile ayaJson, @NotNull Project project) {
    Log.i("[intellij-aya] Hello, this is Aya Language Server inside intellij-aya.");
    var lsp = new AyaLsp();
    lsp.registerLibrary(ayaJson.getParent());
    lsp.recompile();
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
    this.server = new AyaServer();
    this.service = server.getTextDocumentService();
    server.connect(this);
  }

  void fireVfsEvent(List<? extends VFileEvent> events) {
    Seq.wrapJava(events).view()
      .filterIsInstance(VFileContentChangeEvent.class)
      .forEach(ev -> recompile(ev.getFile()));
  }

  void recompile() {
    recompile(() -> service.libraries().flatMap(service::loadLibrary));
  }

  void recompile(@NotNull VirtualFile file) {
    if (isInLibrary(file) && file.getName().endsWith(Constants.AYA_POSTFIX)) {
      Log.i("[intellij-aya] compiling, reason: aya source changed: %s", file.toNioPath());
      recompile(() -> service.loadFile(JB.canonicalize(file)));
    }
  }

  void recompile(@NotNull Supplier<SeqLike<HighlightResult>> compile) {
    compilerPool.execute(() -> compile.get().forEach(this::publishSyntaxHighlight));
  }

  public void registerLibrary(@NotNull VirtualFile library) {
    libraryPathCache.add(library);
    service.registerLibrary(JB.canonicalize(library));
  }

  public boolean isInLibrary(@Nullable VirtualFile file) {
    while (file != null && file.isValid()) {
      if (libraryPathCache.contains(file)) return true;
      file = file.getParent();
    }
    return false;
  }

  public @Nullable LibrarySource sourceFileOf(@NotNull AyaPsiElement element) {
    return service.find(JB.canonicalize(element.getContainingFile().getVirtualFile()));
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

  private @Nullable PsiElement elementAt(@NotNull Project project, @NotNull SourcePos pos) {
    return pos.file().underlying()
      .mapNotNull(path -> VirtualFileManager.getInstance().findFileByNioPath(path))
      .mapNotNull(virtualFile -> PsiManager.getInstance(project).findFile(virtualFile))
      .mapNotNull(psiFile -> psiFile.findElementAt(JB.toRange(pos).getStartOffset()))
      .getOrNull();
  }

  /** Compute highlight for the psi element */
  public @NotNull Option<HighlightResult.Kind> highlight(@NotNull PsiElement element) {
    var range = element.getTextRange();
    var vf = element.getContainingFile().getVirtualFile();
    if (!JB.fileSupported(vf)) return Option.none();
    var path = vf.toNioPath();
    var fileCache = highlightCache.getOrNull(path);
    return fileCache == null ? Option.none() : fileCache.getOption(range);
  }

  @Override public void publishSyntaxHighlight(@NotNull HighlightResult highlight) {
    var path = JB.canonicalize(highlight.uri());
    highlight.symbols().forEach(sym -> {
      var range = JB.toRange(sym.sourcePos().value);
      var kind = sym.kind();
      highlightCache.getOrPut(path, MutableMap::create).put(range, kind);
    });
  }

  @Override public void publishDiagnostics(@NotNull PublishDiagnosticsParams diagnostics) {
    // TODO: report problems in IDEA
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
  }

  @Override public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
    throw new UnsupportedOperationException();
  }

  @Override public void telemetryEvent(Object object) {
    throw new UnsupportedOperationException();
  }
}
