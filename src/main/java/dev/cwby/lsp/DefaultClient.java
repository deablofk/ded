package dev.cwby.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.CompletableFuture;

public class DefaultClient implements LanguageClient {

    @Override
    public void telemetryEvent(Object o) {
        System.out.println("TelemetryEvent: " + o);
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
        System.out.println("Diagnostics Received: " + publishDiagnosticsParams.getUri());
        for (Diagnostic diagnostic : publishDiagnosticsParams.getDiagnostics()) {
            System.out.println(" - " + diagnostic.getMessage());
        }
    }

    @Override
    public void showMessage(MessageParams messageParams) {
        System.out.println("Server Message: " + messageParams.getMessage());
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) {
        System.out.println("Message Request: " + showMessageRequestParams.getMessage());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void logMessage(MessageParams messageParams) {
        System.out.println("Log: " + messageParams.getMessage());
    }

}
