package dev.cwby.editor;

import dev.cwby.graphics.Engine;
import dev.cwby.graphics.FontManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextBuffer {
    public List<StringBuilder> lines = new ArrayList<>();
    public int cursorX = 0;
    public int cursorY = 0;
    public int offsetX = 0;
    public int offsetY = 0;
    public FileChunkLoader fileChunkLoader;
    public File file;

    public TextBuffer() {
        this.lines.add(new StringBuilder());
    }

    public TextBuffer(FileChunkLoader loader) {
        fileChunkLoader = loader;
        loadInitialChunk();
        this.file = loader.getFile();
    }

    private void loadInitialChunk() {
        lines.addAll(fileChunkLoader.loadChunkByByteSize());
    }

    public StringBuilder getCurrentLine() {
        return this.lines.get(this.cursorY);
    }

    public void appendChar(char c) {
        getCurrentLine().append(c);
        this.cursorX++;
    }

    public void insertCharAtCursor(char c) {
        StringBuilder builder = getCurrentLine();
        builder.insert(this.cursorX++, c);
    }

    public void insertTextAtCursor(String text) {
        StringBuilder builder = getCurrentLine();
        builder.insert(this.cursorX, text);
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
        int visibleLines = (int) ((Engine.getHeight() - FontManager.getLineHeight()) / FontManager.getLineHeight());
        this.cursorY = Math.min(Math.max(0, y), this.lines.size() - 1);
        this.cursorX = Math.min(Math.max(0, x), getCurrentLine().length() - 1);

        if (cursorY > offsetY + visibleLines) {
            offsetY++;
        }

        if (cursorY < offsetY) {
            offsetY = cursorY;
        }
    }

}
