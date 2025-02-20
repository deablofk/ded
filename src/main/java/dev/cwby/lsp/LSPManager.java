package dev.cwby.lsp;

import dev.cwby.FileUtils;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.pkgs.PackageData;
import dev.cwby.pkgs.PackageManager;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LSPManager {
    private static final LSPClientListener LSP_LISTENER = new LSPClientListener();
    private static final Map<ScratchBuffer, LSPClient> ATTACHED_LSP = new HashMap<>();

    private static LSPClient startLSPClient(Launcher<LanguageServer> launcher, String projectRoot) throws ExecutionException, InterruptedException {
        launcher.startListening();
        var server = launcher.getRemoteProxy();
        var client = new LSPClient(server);

        InitializeParams initParams = new InitializeParams();
        initParams.setProcessId((int) ProcessHandle.current().pid());
        initParams.setRootPath(client.getFileSchema() + projectRoot);
        initParams.setCapabilities(new ClientCapabilities());

        server.initialize(initParams).get();
        server.initialized(new InitializedParams());
        return client;
    }

    private static ProcessBuilder createCommand(PackageData pkg) {
        String command = switch (pkg.source.type) {
            case COMMAND -> pkg.source.executable;
            default -> PackageManager.INTERNALS_DIR + pkg.name + "/" + pkg.source.executable;
        };
        return new ProcessBuilder(command.split(" "));
    }

    public static void initializeServer(ScratchBuffer buffer, PackageData pkg) {
        File root = FileUtils.findProjectRoot(new File(buffer.getFilepath()), pkg.trigger.projectRoot);
        try {
            ProcessBuilder pb = createCommand(pkg);
            pb.redirectErrorStream(true);
            var process = pb.start();
            lspErrorLogger(process.getErrorStream(), pkg.name);
            Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(LSP_LISTENER, process.getInputStream(), process.getOutputStream());
            LSPClient client = startLSPClient(launcher, root.getAbsolutePath());
            ATTACHED_LSP.put(buffer, client);
            client.sendDidOpen(buffer);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void lspErrorLogger(InputStream errorStream, String lspName) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("[" + lspName + " Error] " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public static LSPClient getLSPClient(ScratchBuffer buffer) {
        return ATTACHED_LSP.get(buffer);
    }

    public static void closeAllLsp() {
        try {
            for (LSPClient client : ATTACHED_LSP.values()) {
                client.close();
            }
        } catch (Exception e) {
        }
        System.exit(0);
    }
}
