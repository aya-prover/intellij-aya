package org.aya.intellij.lsp;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
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

public record AyaLSP(
  @NotNull AyaServer server,
  @NotNull MutableMap<Path, MutableMap<TextRange, HighlightResult.Kind>> highlightCache,
  @NotNull MutableSet<VirtualFile> libraryPathCache,
  @NotNull ExecutorService compilerPool
) implements AyaLanguageClient {
  public AyaLSP() {
    this(new AyaServer(), MutableMap.create(), MutableSet.create(), Executors.newFixedThreadPool(1));
    server.connect(this);
  }

  void fireVfsEvent(List<? extends VFileEvent> events) {
    Seq.wrapJava(events).view()
      .filterIsInstance(VFileContentChangeEvent.class)
      .forEach(ev -> {
        var file = ev.getFile();
        if (isInLibrary(file) && file.getName().endsWith(Constants.AYA_POSTFIX)) {
          System.out.println("[intellij-aya] compiling, reason: aya source changed: " + file.getUrl());
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

  @Override public void logMessage(MessageParams message) {
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
