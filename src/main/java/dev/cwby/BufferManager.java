package dev.cwby;

import dev.cwby.editor.FileChunkLoader;
import dev.cwby.editor.TextBuffer;
import dev.cwby.lsp.LSPManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BufferManager {
    public static TextBuffer emptyBuffer = new TextBuffer();
    public static Map<String, TextBuffer> buffers = new HashMap<>();
    public static boolean shouldOpenEmptyBuffer = true;

    public static TextBuffer getBuffer(String absolutePath) {
        return buffers.get(absolutePath);
    }

    public static TextBuffer addEmptyBuffer() {
        return new TextBuffer();
    }

    public static TextBuffer openFileBuffer(String filePath) {
        return openFileBuffer(new File(filePath));
    }

    public static TextBuffer openFileBuffer(File file) {
        if (file.exists()) {
            if (buffers.containsKey(file.getAbsolutePath())) {
                return buffers.get(file.getAbsolutePath());
            }
            System.out.println(file.getAbsolutePath());
            TextBuffer textBuffer = new TextBuffer(new FileChunkLoader(file, 64 * 1024));
            LSPManager.initializeServer(file.getAbsolutePath(), textBuffer);
            buffers.put(file.getAbsolutePath(), textBuffer);
            return textBuffer;
        }
        return addEmptyBuffer();
    }
}
