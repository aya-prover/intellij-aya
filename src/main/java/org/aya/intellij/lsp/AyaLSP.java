package org.aya.intellij.lsp;

import org.aya.lsp.models.HighlightResult;
import org.aya.lsp.server.AyaLanguageClient;
import org.aya.lsp.server.AyaServer;
import org.aya.lsp.server.AyaService;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public record AyaLSP(@NotNull AyaServer server) implements AyaLanguageClient {
  public AyaLSP() {
    this(new AyaServer());
    server.connect(this);
  }

  public @NotNull AyaService service() {
    return ((AyaService) server.getTextDocumentService());
  }

  @Override public void publishSyntaxHighlight(HighlightResult highlight) {
    // TODO: semantic highlight in IDEA
  }

  @Override public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
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
