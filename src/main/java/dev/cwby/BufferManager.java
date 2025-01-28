package dev.cwby;

import dev.cwby.editor.TextBuffer;

import java.util.ArrayList;
import java.util.List;

public class BufferManager {
    public static int actualBufferId = 0;
    public static List<TextBuffer> buffers = new ArrayList<>();

    public static TextBuffer getActualBuffer() {
        return buffers.get(actualBufferId);
    }

    public static void setActualBuffer(int id) {
        actualBufferId = id;
    }

    public static TextBuffer getBuffer(int id) {
        return buffers.get(id);
    }

    public static void addBuffer(TextBuffer buffer) {
        buffers.add(buffer);
    }

    public static TextBuffer addEmptyBuffer() {
        TextBuffer textBuffer = new TextBuffer();
        buffers.add(textBuffer);
        return textBuffer;
    }
}
