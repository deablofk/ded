package dev.cwby;

import dev.cwby.editor.FileChunkLoader;
import dev.cwby.editor.TextBuffer;
import dev.cwby.lsp.LSPManager;
import dev.cwby.pkgs.PackageCategory;
import dev.cwby.pkgs.PackageData;
import dev.cwby.pkgs.PackageManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BufferManager {
    private static final Map<String, TextBuffer> BUFFERS = new HashMap<>();

    public static TextBuffer addEmptyBuffer() {
        return new TextBuffer();
    }

    public static TextBuffer openFileBuffer(String filePath) {
        return openFileBuffer(new File(filePath));
    }

    public static TextBuffer openFileBuffer(File file) {
        if (!file.exists()) {
            return addEmptyBuffer();
        }
        if (BUFFERS.containsKey(file.getAbsolutePath())) {
            return BUFFERS.get(file.getAbsolutePath());
        }
        TextBuffer textBuffer = new TextBuffer(new FileChunkLoader(file, 64 * 1024));
        // TODO: autocmd before-open and after-open
        for (PackageData server : PackageManager.filterCategory(PackageCategory.LSP)) {
            System.out.println("loading LSP");
            if (server.trigger.filetypes.contains(textBuffer.getFileType())) {
                LSPManager.initializeServer(textBuffer, server);
            }
        }
        BUFFERS.put(file.getAbsolutePath(), textBuffer);
        return textBuffer;
    }
}
