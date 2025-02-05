package dev.cwby.lsp;

import dev.cwby.editor.TextBuffer;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class LSPManager {
    private static final String JDTLS_DIR = "/home/cwby/jdtls/bin";
    private static LanguageServer languageServer;
    private static DefaultClient client = new DefaultClient();
    private static boolean initialized = false;
    private static Process process;

    private static final Set<String> FT_ROOT_IDENTIFIER = Set.of("pom.xml", "build.gradle", "build.gradle.kts", ".classpath", ".project", ".gitignore");

    public static File findProjectRoot(File file) {
        File dir = file.isDirectory() ? file : file.getParentFile();
        while (dir != null) {
            for (String indicator : FT_ROOT_IDENTIFIER) {
                if (new File(dir, indicator).exists()) {
                    return dir;
                }
            }
            dir = dir.getParentFile();
        }

        return null;
    }

    private static void initializeClient(String rootPath, InputStream serverIn, OutputStream serverOut) {
        Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(client, serverIn, serverOut);
        launcher.startListening();
        languageServer = launcher.getRemoteProxy();

        InitializeParams initParams = new InitializeParams();
        initParams.setProcessId((int) ProcessHandle.current().pid());
//        initParams.setRootPath("file:///home/cwby/programming/cwby/ded/");
        initParams.setRootPath("file://" + rootPath);
        initParams.setCapabilities(new ClientCapabilities());

        try {
            InitializeResult initialize = languageServer.initialize(initParams).get();
            System.out.println("LSP initialized: " + initialize.getCapabilities());

            languageServer.initialized(new InitializedParams());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initializeServer(String javaFilePath, TextBuffer textBuffer) {
        File root = findProjectRoot(new File(javaFilePath));
        if (root == null) {
            System.out.println("Could not find project root for file: (" + javaFilePath + "), only syntax will be reported");
        }
        try {
            if (!initialized) {
                ProcessBuilder pb = new ProcessBuilder(JDTLS_DIR + "/jdtls", "-data", "/home/cwby/.cache/jdtls-workspace");
                process = pb.start();
                pb.redirectErrorStream(true);

                // for some reason this prevent the application of being stuck, probably because the buffer dont have space at certain time
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.err.println("[JDTLS ERROR] " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                initializeClient(root.getAbsolutePath(), process.getInputStream(), process.getOutputStream());
            }

            initialized = true;

            languageServer.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem("file://" + javaFilePath, "java", 1, textBuffer.getSourceCode())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LanguageServer getLanguageServer() {
        return languageServer;
    }

    public static TextDocumentService getTextDocumentService() {
        return getLanguageServer().getTextDocumentService();
    }

    public static List<CompletionItem> requestCompletion(String fileUri, int line, int character) {
        Position position = new Position(line, character);

        TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier("file://" + fileUri);

        CompletionParams completionParams = new CompletionParams(textDocumentIdentifier, position);

        try {
            Either<List<CompletionItem>, CompletionList> either = languageServer.getTextDocumentService().completion(completionParams).get();
            return either.isLeft() ? either.getLeft() : either.getRight().getItems();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return List.of();
    }

    public static List<CompletionItem> onDotPressed(String fileUri, int line, int character) {
        return requestCompletion(fileUri, line, character);
    }

    private static int version = 1;

    public static void sendDidChangeNotification(TextBuffer buffer) {
        VersionedTextDocumentIdentifier documentIdentifier = new VersionedTextDocumentIdentifier();
        documentIdentifier.setUri("file://" + buffer.file.getAbsolutePath());
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

    public static List<Location> getDefinitions(String fileUri, int line, int character) {
        Position position = new Position(line, character);
        TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier("file://" + fileUri);
        DefinitionParams params = new DefinitionParams(textDocumentIdentifier, position);

        try {
            Either<List<? extends Location>, List<? extends LocationLink>> eitherDefinition = getTextDocumentService().definition(params).get();

            List<Location> locations = new ArrayList<>();

            if (eitherDefinition.isLeft()) {
                locations.addAll(eitherDefinition.getLeft());
            } else {
                for (LocationLink link : eitherDefinition.getRight()) {
                    locations.add(new Location(link.getTargetUri(), link.getTargetRange()));
                }
            }

            return locations;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeAllLsp() {
        try {
            if (languageServer != null) {
                languageServer.shutdown().get();
                languageServer.exit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (process != null && process.isAlive()) {
                process.getOutputStream().close();
                process.getInputStream().close();
                process.getErrorStream().close();
                process.destroyForcibly();
                process.waitFor();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // force anyway
        System.exit(0);
    }
}
