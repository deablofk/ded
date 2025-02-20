package dev.cwby.lsp;

import dev.cwby.editor.ScratchBuffer;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LSPClient {

    public LanguageServer languageServer;
    public String fileSchema = "file://";

    public LSPClient(LanguageServer languageServer) {
        this.languageServer = languageServer;
    }

    public void setFileSchema(String fileSchema) {
        this.fileSchema = fileSchema;
    }

    public String getFileSchema() {
        return fileSchema;
    }

    public LanguageServer getLanguageServer() {
        return languageServer;
    }

    public TextDocumentService getTextDocumentService() {
        return getLanguageServer().getTextDocumentService();
    }

    public void close() throws ExecutionException, InterruptedException {
        var server = getLanguageServer();
        server.shutdown().get();
        server.exit();
    }

    public List<CompletionItem> requestCompletion(ScratchBuffer buffer) {
        var position = new Position(buffer.cursorY, buffer.cursorX);
        var textDocumentIdentifier = new TextDocumentIdentifier(fileSchema + buffer.getFilepath());
        var completionParams = new CompletionParams(textDocumentIdentifier, position);

        try {
            Either<List<CompletionItem>, CompletionList> either = getTextDocumentService().completion(completionParams).get();
            return either.isLeft() ? either.getLeft() : either.getRight().getItems();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return List.of();
    }

    public List<Location> requestDefinitions(ScratchBuffer buffer) {
        var params = new DefinitionParams(new TextDocumentIdentifier(fileSchema + buffer.getFilepath()), new Position(buffer.cursorY, buffer.cursorX));

        try {
            var eitherDefinition = getTextDocumentService().definition(params).get();
            if (eitherDefinition.isLeft()) {
                return (List<Location>) eitherDefinition.getLeft();
            }
            return eitherDefinition.getRight().stream().map(x -> new Location(x.getTargetUri(), x.getTargetRange())).toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    // remove this
    private int version = 1;

    public void sendDidChange(ScratchBuffer buffer) {
        VersionedTextDocumentIdentifier documentIdentifier = new VersionedTextDocumentIdentifier();
        documentIdentifier.setUri(fileSchema + buffer.getFilepath());
        documentIdentifier.setVersion(version++);

        List<TextDocumentContentChangeEvent> contentChanges = new ArrayList<>();
        TextDocumentContentChangeEvent changeEvent = new TextDocumentContentChangeEvent();
        int size = buffer.lines.size() - 1;
        // TODO: modify this to update only the line range instead of entire document
        changeEvent.setRange(new Range(new Position(0, 0), new Position(size, 0)));
        changeEvent.setText(buffer.getSourceCode());

        contentChanges.add(changeEvent);

        DidChangeTextDocumentParams params = new DidChangeTextDocumentParams(documentIdentifier, contentChanges);

        getTextDocumentService().didChange(params);
    }

    public void sendDidOpen(ScratchBuffer buffer) {
        getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(fileSchema + buffer.getFilepath(), buffer.getFileType(), 1, buffer.getSourceCode())));
    }

}

