package dev.cwby.editor;

import org.eclipse.lsp4j.Range;

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
        getCurrentLine().insert(this.cursorX++, c);
    }

    public void insertCharAtCursor(char c) {
        StringBuilder builder = getCurrentLine();
        builder.insert(this.cursorX++, c);
    }

    public void insertTextAtCursor(String text) {
        StringBuilder builder = getCurrentLine();
        builder.insert(this.cursorX, text);
        cursorX += text.length();
    }

    public void pasteText(String text) {
        StringBuilder builder = getCurrentLine();
        String[] parts = text.split("\n", -1);

        builder.insert(cursorX, parts[0]);
        cursorX += parts[0].length();

        for (int i = 1; i < parts.length; i++) {
            StringBuilder newLine = new StringBuilder(parts[i]);

            if (i == 1) {
                String remaining = builder.substring(cursorX);
                builder.setLength(cursorX);
                newLine.insert(0, remaining);
            }

            lines.add(++cursorY, newLine);
            cursorX = newLine.length();
        }
    }

    public void replaceTextInRange(Range range, String text) {
        int startLine = range.getStart().getLine();
        int startChar = range.getStart().getCharacter();
        int endLine = range.getEnd().getLine();
        int endChar = range.getEnd().getCharacter();

        if (startLine == endLine) {
            StringBuilder line = lines.get(startLine);
            line.replace(startChar, endChar, text);
            cursorX = startChar + text.length();
        } else {
            StringBuilder firstLine = lines.get(startLine);
            firstLine.replace(startChar, firstLine.length(), text);

            for (int lineX = startLine + 1; lineX < endLine; lineX++) {
                lines.set(lineX, new StringBuilder());
            }

            StringBuilder lastLine = lines.get(endLine);
            lastLine.replace(0, endChar, "");
            lastLine.insert(0, text);
        }
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

    public void removeCharAfterCursor() {
        if (cursorX < getCurrentLine().length()) {
            getCurrentLine().deleteCharAt(cursorX);
        } else if (cursorY < lines.size() - 1) {
            StringBuilder nextLine = lines.remove(cursorY + 1);
            getCurrentLine().append(nextLine);
        }
    }

    // not working correctly for removing next words
    public void removeWordAfterCursor() {
        StringBuilder currentLine = getCurrentLine();
        while (cursorX < currentLine.length() && currentLine.charAt(cursorX) == ' ') {
            currentLine.deleteCharAt(cursorX);
        }

        while (cursorX < currentLine.length()) {
            char c = currentLine.charAt(cursorX);

            if (c == ' ' || Character.isUpperCase(c) || !Character.isLetter(c)) {
                break;
            }

            currentLine.deleteCharAt(cursorX);
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

    public void smartNewLine() {
        StringBuilder currentLine = lines.get(cursorY);
        int indentLevel = 0;

        while (indentLevel < currentLine.length() && Character.isWhitespace(currentLine.charAt(indentLevel))) {
            indentLevel++;
        }

        String indentation = currentLine.substring(0, indentLevel);

        if (cursorX > 0 && cursorX <= currentLine.length()) {
            char lastChar = currentLine.charAt(cursorX - 1);

            if (lastChar == '{' || lastChar == '(' || lastChar == ':') {
                indentation += "    ";
            }
        }

        lines.add(cursorY + 1, new StringBuilder(indentation));
        cursorX = indentation.length();
        cursorY++;
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

    public void moveCursor(int x, int y, int visibleLines) {
        this.cursorY = Math.min(Math.max(0, y), this.lines.size() - 1);
        this.cursorX = Math.min(Math.max(0, x), getCurrentLine().length());

        if (cursorY >= offsetY + visibleLines) {
            offsetY = cursorY - visibleLines + 1;
        } else if (cursorY < offsetY) {
            offsetY = cursorY;
        }
    }

    public void moveCursorLeft(int visibleLines) {
        moveCursor(--cursorX, cursorY, visibleLines);
    }

    public void moveCursorRight(int visibleLines) {
        moveCursor(++cursorX, cursorY, visibleLines);
    }

    public void moveCursorUp(int visibleLines) {
        moveCursor(cursorX, --cursorY, visibleLines);
    }

    public void moveCursorDown(int visibleLines) {
        moveCursor(cursorX, ++cursorY, visibleLines);
    }

    public void moveCursorHalfUp(int visibleLines) {
        moveCursor(cursorX, cursorY - visibleLines / 2, visibleLines);
    }

    public void moveCursorHalfDown(int visibleLines) {
        moveCursor(cursorX, cursorY + visibleLines / 2, visibleLines);
    }

    public void moveToFirstNonWhitespaceChar() {
        char c = getCurrentLine().toString().trim().charAt(0);
        int index = getCurrentLine().indexOf(c + "");
        gotoPosition(index, cursorY);
    }

    public void moveToLastNonWhitespaceChar() {
        String trimed = getCurrentLine().toString().trim();
        char c = trimed.charAt(trimed.length() - 1);
        int index = getCurrentLine().indexOf(c + "");
        gotoPosition(index + 1, cursorY);
    }

    public void moveNextWord() {
        StringBuilder currentLine = getCurrentLine();
        int len = currentLine.length();

        while (cursorX < len && Character.isLetterOrDigit(currentLine.charAt(cursorX))) {
            cursorX++;
        }

        while (cursorX < len && !Character.isLetterOrDigit(currentLine.charAt(cursorX))) {
            cursorX++;
        }

        cursorX = Math.min(cursorX, len);
    }

    public void movePreviousWord() {
        StringBuilder currentLine = getCurrentLine();

        while (cursorX > 0 && !Character.isLetterOrDigit(currentLine.charAt(cursorX - 1))) {
            cursorX--;
        }

        while (cursorX > 0 && Character.isLetterOrDigit(currentLine.charAt(cursorX - 1))) {
            cursorX--;
        }

        cursorX = Math.max(cursorX, 0);
    }

    public void gotoPosition(int x, int y) {
        cursorY = Math.min(y, lines.size());
        cursorX = Math.min(x, getCurrentLine().length());
    }

    public String getSourceCode() {
        var code = new StringBuilder();
        for (var line : lines) {
            code.append(line).append("\n");
        }
        return code.toString();
    }

    public List<StringBuilder> getLines() {
        return lines;
    }

    public String getRegion(int startChar, int startLine, int endChar, int endLine) {
        StringBuilder region = new StringBuilder();

        if (startLine > endLine || (startLine == endLine && startChar > endChar)) {
            int tempChar = startChar;
            int tempLine = startLine;
            startChar = endChar;
            startLine = endLine;
            endChar = tempChar;
            endLine = tempLine;
        }

        for (int line = startLine; line <= endLine; line++) {
            String content = String.valueOf(lines.get(line));

            if (line == startLine && line == endLine) {
                region.append(content, startChar, endChar).append("\n");
            } else if (line == startLine) {
                region.append(content.substring(startChar)).append("\n");
            } else if (line == endLine) {
                if (endChar + 1 < content.length()) {
                    region.append(content, 0, endChar + 1);
                } else {
                    region.append(content, 0, endChar);
                }
            } else {
                region.append(content).append("\n");
            }
        }

        return region.toString();
    }

    public void deleteRegion(int startChar, int startLine, int endChar, int endLine) {
        if (startLine > endLine || (startLine == endLine && startChar > endChar)) {
            int tempChar = startChar;
            int tempLine = startLine;
            startChar = endChar;
            startLine = endLine;
            endChar = tempChar;
            endLine = tempLine;
        }

        for (int line = startLine; line <= endLine; line++) {
            StringBuilder content = lines.get(line);

            if (line == startLine && line == endLine) {
                content.delete(startChar, endChar);
            } else if (line == startLine) {
                content.delete(startChar, content.length());
            } else if (line == endLine) {
                content.delete(0, endChar);
            } else {
                lines.set(line, new StringBuilder()); // Clear intermediate lines
            }
        }

        // Merge lines if deletion spanned multiple lines
        if (startLine < lines.size() - 1 && startLine != endLine) {
            lines.get(startLine).append(lines.get(endLine));
            lines.remove(endLine);
        }

        // Adjust cursor position
        cursorX = startChar;
        cursorY = startLine;
    }

}
