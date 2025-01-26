package dev.cwby.editor;

import java.util.ArrayList;
import java.util.List;

public class TextBuffer {
    public final List<StringBuilder> lines = new ArrayList<>();
    public int cursorX = 0;
    public int cursorY = 0;

    public TextBuffer() {
        this.lines.add(new StringBuilder());
    }

    public StringBuilder getCurrentLine() {
        return this.lines.get(this.cursorY);
    }

    public void appendText(String text) {
        getCurrentLine().append(text);
    }

    public void appendChar(char c) {
        getCurrentLine().append(c);
        cursorX++;
    }

    public void newLine() {
        getCurrentLine().append("\n");
        cursorY++;
        cursorX = 0;
        lines.add(new StringBuilder());
    }
}
