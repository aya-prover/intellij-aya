package org.aya.intellij.lsp;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import kala.collection.mutable.MutableMap;
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
import java.util.concurrent.CompletableFuture;

public record AyaLSP(
  @NotNull AyaServer server,
  @NotNull MutableMap<Path, MutableMap<TextRange, HighlightResult.Kind>> highlightCache
) implements AyaLanguageClient {
  public AyaLSP() {
    this(new AyaServer(), MutableMap.create());
    server.connect(this);
  }

  public @NotNull AyaService service() {
    return server.getTextDocumentService();
  }

  public @Nullable HighlightResult.Kind highlight(@NotNull PsiFile file, @NotNull TextRange range) {
    var vf = file.getVirtualFile();
    if (!JB.fileSupported(vf)) return null;
    var path = vf.toNioPath();
    return highlightCache
      .getOrPut(path, MutableMap::create)
      .getOrNull(range);
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
