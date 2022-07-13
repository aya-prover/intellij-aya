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
import com.intellij.psi.PsiFile;
import kala.collection.Seq;
import kala.collection.mutable.MutableMap;
import kala.collection.mutable.MutableSet;
import org.aya.generic.Constants;
import org.aya.lsp.models.HighlightResult;
import org.aya.lsp.server.AyaLanguageClient;
import org.aya.lsp.server.AyaServer;
import org.aya.lsp.server.AyaService;
import org.aya.lsp.utils.Log;
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

public record AyaLsp(
  @NotNull AyaServer server,
  @NotNull MutableMap<Path, MutableMap<TextRange, HighlightResult.Kind>> highlightCache,
  @NotNull MutableSet<VirtualFile> libraryPathCache,
  @NotNull ExecutorService compilerPool
) implements AyaLanguageClient {
  private static final @NotNull Key<AyaLsp> AYA_LSP = Key.create("intellij.aya.lsp");
  private static final @NotNull Logger LOG = Logger.getInstance(AyaLsp.class);

  public static void start(@NotNull VirtualFile ayaJson, @NotNull Project project) {
    var lsp = new AyaLsp();
    lsp.registerLibrary(ayaJson.getParent());
    project.putUserData(AYA_LSP, lsp);
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
      @Override public void after(@NotNull List<? extends VFileEvent> events) {
        lsp.fireVfsEvent(events);
      }
    });
    Log.i("[intellij-aya] Hello, this is Aya Language Server inside intellij-aya.");
  }

  public static @Nullable AyaLsp of(@NotNull Project project) {
    return project.getUserData(AYA_LSP);
  }

  public AyaLsp() {
    this(new AyaServer(), MutableMap.create(), MutableSet.create(), Executors.newFixedThreadPool(1));
    server.connect(this);
  }

  void fireVfsEvent(List<? extends VFileEvent> events) {
    Seq.wrapJava(events).view()
      .filterIsInstance(VFileContentChangeEvent.class)
      .forEach(ev -> {
        var file = ev.getFile();
        if (isInLibrary(file) && file.getName().endsWith(Constants.AYA_POSTFIX)) {
          Log.i("[intellij-aya] compiling, reason: aya source changed: %s", file.getUrl());
          compilerPool.execute(() -> service().loadFile(JB.canonicalize(file))
            .forEach(this::publishSyntaxHighlight));
        }
      });
  }

  public @NotNull AyaService service() {
    return server.getTextDocumentService();
  }

  public void registerLibrary(@NotNull VirtualFile library) {
    libraryPathCache.add(library);
    service().registerLibrary(JB.canonicalize(library));
  }

  public boolean isInLibrary(@Nullable VirtualFile file) {
    while (file != null && file.isValid()) {
      if (libraryPathCache.contains(file)) return true;
      file = file.getParent();
    }
    return false;
  }

  public @Nullable HighlightResult.Kind highlight(@NotNull PsiFile file, @NotNull TextRange range) {
    var vf = file.getVirtualFile();
    if (!JB.fileSupported(vf)) return null;
    var path = vf.toNioPath();
    var fileCache = highlightCache.getOrNull(path);
    return fileCache == null ? null : fileCache.getOrNull(range);
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
