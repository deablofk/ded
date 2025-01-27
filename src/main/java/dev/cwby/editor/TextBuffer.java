package dev.cwby.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextBuffer {
    public final List<StringBuilder> lines = new ArrayList<>();
    public int cursorX = 0;
    public int cursorY = 0;
    public File file;

    public TextBuffer() {
        this.lines.add(new StringBuilder());
    }

    public TextBuffer(List<String> lines, File file) {
        this.file = file;
        lines.stream().map(StringBuilder::new).forEach(this.lines::add);
    }

    public StringBuilder getCurrentLine() {
        return this.lines.get(this.cursorY);
    }

    public void appendText(String text) {
        getCurrentLine().append(text);
    }

    public void appendChar(char c) {
        getCurrentLine().append(c);
        this.cursorX++;
    }

    public void insertCharAtCursor(char c) {
        StringBuilder builder = getCurrentLine();
        builder.insert(this.cursorX++, c);
    }

    public void removeChar() {
        if (cursorX > 0) {
            getCurrentLine().deleteCharAt(cursorX - 1);
            cursorX--;
        } else if (cursorY > 0) {
            StringBuilder currentLine = lines.remove(cursorY);
            cursorY--;
            cursorX = lines.get(cursorY).length();
            lines.get(cursorY).append(currentLine);
        }
    }

    public void newLineUp() {
        if (cursorY < 0) {
            cursorY = 0;
        }
        cursorX = 0;
        lines.add(cursorY, new StringBuilder());
    }

    public void newLineDown() {
        cursorY++;
        cursorX = 0;
        lines.add(cursorY, new StringBuilder());
    }

    public void newLine() {
        cursorY++;
        cursorX = 0;
        lines.add(cursorY, new StringBuilder());
    }

    public void deleteCurrentLine() {
        if (lines.isEmpty()) return; // If there are no lines, do nothing

        if (lines.size() == 1) {
            getCurrentLine().setLength(0);
            cursorX = 0;
        } else {
            lines.remove(cursorY);
            if (cursorY > 0) cursorY--;
        }
    }

    public void moveCursor(int x, int y) {
        this.cursorY = Math.min(Math.max(0, y), this.lines.size() - 1);
        this.cursorX = Math.min(Math.max(0, x), getCurrentLine().length() - 1);
    }

    public String getSource() {
        StringBuilder allLines = new StringBuilder();
        for (StringBuilder line : lines) {
            allLines.append(line).append("\n");
        }
        return allLines.toString();
    }

    public List<String> getLines() {
        return lines.stream().map(String::new).toList();
    }
}
