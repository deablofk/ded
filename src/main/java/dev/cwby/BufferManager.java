package dev.cwby;

import dev.cwby.editor.FileChunkLoader;
import dev.cwby.editor.TextBuffer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BufferManager {
    public static List<TextBuffer> buffers = new ArrayList<>();
    public static boolean shouldOpenEmptyBuffer = true;

    public static TextBuffer getBuffer(int id) {
        return buffers.get(id);
    }

    public static TextBuffer addEmptyBuffer() {
        TextBuffer textBuffer = new TextBuffer();
        buffers.add(textBuffer);
        return textBuffer;
    }

    public static TextBuffer openFileBuffer(String filePath) {
        return openFileBuffer(new File(filePath));
    }

    public static TextBuffer openFileBuffer(File file) {
        if (file.exists()) {
            System.out.println(file.getAbsolutePath());
            TextBuffer textBuffer = new TextBuffer(new FileChunkLoader(file, 10240));
            buffers.add(textBuffer);
            return textBuffer;
        }
        return addEmptyBuffer();
    }
}
