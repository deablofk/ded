package dev.cwby.editor;

import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

public class ScratchBuffer {
    public List<StringBuilder> lines = new ArrayList<>();
    public int cursorX = 0;
    public int cursorY = 0;
    public int offsetX = 0;
    public int offsetY = 0;
    public int visibleLines = 0;
    public String filepath = null;
    public String fileType = null;

    public ScratchBuffer() {
        this.lines.add(new StringBuilder());
    }

    public StringBuilder getCurrentLine() {
        return this.lines.get(this.cursorY);
    }

    public List<StringBuilder> getLines() {
        return lines;
    }

    public void setLines(List<StringBuilder> lines) {
        this.lines = lines;
    }

    public String getSourceCode() {
        var code = new StringBuilder();
        lines.forEach(x -> code.append(x).append('\n'));
        return code.toString();
    }

    public int getVisibleLines() {
        return visibleLines;
    }

    public void setVisibleLines(int visibleLines) {
        this.visibleLines = visibleLines;

    }

    public void appendChar(char c) {
        getCurrentLine().insert(this.cursorX++, c);
    }

    public void insertTextAtCursor(String text) {
        StringBuilder builder = getCurrentLine();
        builder.insert(this.cursorX, text);
        cursorX += text.length();
    }

    public void pasteText(String text) {
        if (text == null) {
            return;
        }
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
        int level = calculateIndentation(getCurrentLine().toString()) * 4;
        StringBuilder newLine = new StringBuilder(" ".repeat(level));
        cursorX = newLine.length();
        lines.add(cursorY, newLine);
    }

    public void newLineDown() {
        int level = calculateIndentation(getCurrentLine().toString()) * 4;
        StringBuilder newLine = new StringBuilder(" ".repeat(level));
        cursorX = newLine.length();
        lines.add(++cursorY, newLine);
    }

    public int countLeadingIndentation(String line) {
        int spaceCount = 0;
        int tabCount = 0;
        while (spaceCount < line.length() && (line.charAt(spaceCount) == ' ' || line.charAt(spaceCount) == '\t')) {
            if (line.charAt(spaceCount) == ' ') {
                spaceCount++;
            } else if (line.charAt(spaceCount) == '\t') {
                tabCount++;
                spaceCount++;
            }
        }

        return tabCount * 4 + (spaceCount / 4);
    }

    public int calculateIndentation(String previousLine) {
        int indent = countLeadingIndentation(previousLine);

        if (previousLine.endsWith("{")) {
            indent++;
        }

        return Math.max(indent, 0);
    }

    public void smartNewLine() {
        StringBuilder currentLine = lines.get(cursorY);
        int lineLength = currentLine.length();
        int level = calculateIndentation(currentLine.toString()) * 4;
        System.out.println(level);
        String indentation = " ".repeat(level);
        StringBuilder newLine = new StringBuilder(indentation);
        if (cursorX >= lineLength - 1) {
            lines.add(++cursorY, newLine);
            cursorX = newLine.length();
        } else {
            lines.set(cursorY, new StringBuilder(currentLine.substring(0, cursorX)));
            newLine.append(currentLine.substring(cursorX));
            lines.add(++cursorY, new StringBuilder(newLine));
            cursorX = level;
        }
    }

    public void deleteCurrentLine() {
        if (lines.size() == 1) {
            cursorX = 0;
            getCurrentLine().setLength(0);
            return;
        }

        if (lines.size() - 1 == cursorY) {
            lines.remove(cursorY);
            if (cursorY > 0) {
                cursorY--;
            }
        } else {
            lines.remove(cursorY);
        }
    }

    public void moveCursor(int x, int y) {
        this.cursorY = Math.min(Math.max(0, y), this.lines.size() - 1);
        this.cursorX = Math.min(Math.max(0, x), getCurrentLine().length());

        if (cursorY >= offsetY + visibleLines) {
            offsetY = cursorY - visibleLines + 1;
        } else if (cursorY < offsetY) {
            offsetY = cursorY;
        }
    }

    public void moveCursorLeft() {
        moveCursor(--cursorX, cursorY);
    }

    public void moveCursorRight() {
        moveCursor(++cursorX, cursorY);
    }

    public void moveCursorUp() {
        moveCursor(cursorX, --cursorY);
    }

    public void moveCursorDown() {
        moveCursor(cursorX, ++cursorY);
    }

    public void moveCursorHalfUp() {
        moveCursor(cursorX, cursorY - visibleLines / 2);
    }

    public void moveCursorHalfDown() {
        moveCursor(cursorX, cursorY + visibleLines / 2);
    }

    public void moveToFirstNonWhitespaceChar() {
        char c = getCurrentLine().toString().trim().charAt(0);
        int index = getCurrentLine().indexOf(c + "");
        if (index != -1) {
            gotoPosition(index, cursorY);
        }
    }

    public void moveToLastChar() {
        gotoPosition(getCurrentLine().length(), cursorY);
    }

    private int[] getWordBounds(boolean forward) {
        StringBuilder currentLine = getCurrentLine();
        int len = currentLine.length();
        int start = cursorX;
        int end = cursorX;

        if (forward) {
            while (end < len && Character.isLetterOrDigit(currentLine.charAt(end))) {
                end++;
            }
            while (end < len && !Character.isLetterOrDigit(currentLine.charAt(end))) {
                end++;
            }
        } else {
            while (start > 0 && !Character.isLetterOrDigit(currentLine.charAt(start - 1))) {
                start--;
            }
            while (start > 0 && Character.isLetterOrDigit(currentLine.charAt(start - 1))) {
                start--;
            }
        }

        return new int[]{start, end};
    }

    public void moveNextWord() {
        cursorX = getWordBounds(true)[1];
    }

    public void movePreviousWord() {
        cursorX = getWordBounds(false)[0];
    }

    public void removeNextWord() {
        int[] bounds = getWordBounds(true);
        getCurrentLine().delete(bounds[0], bounds[1]);
    }

    public void removePreviousWord() {
        int[] bounds = getWordBounds(false);
        getCurrentLine().delete(bounds[0], bounds[1]);
        cursorX = bounds[0];
    }

    public void gotoPosition(int x, int y) {
        cursorY = Math.min(y, lines.size());
        cursorX = Math.min(x, getCurrentLine().length());
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
                if (endChar + 1 <= content.length()) {
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

        List<Integer> linesToRemove = new ArrayList<>();
        for (int line = startLine; line <= endLine; line++) {
            StringBuilder content = lines.get(line);

            if (line == startLine && line == endLine) {
                content.delete(startChar, endChar + 1);
            } else if (line == startLine) {
                content.delete(startChar, content.length());
            } else if (line == endLine) {
                content.delete(0, endChar + 1);
            } else {
                linesToRemove.add(line);
            }
        }

        for (Integer line : linesToRemove) {
            lines.remove(line.intValue());
        }

        if (startLine < lines.size() - 1 && startLine != endLine) {
            lines.get(startLine).append(lines.get(endLine));
            lines.remove(endLine);
        }

        cursorX = startChar;
        cursorY = startLine;
    }

    public String getFilepath() {
        return filepath;
    }

    public List<int[]> searchWordUnderCursor() {
        List<int[]> positions = new ArrayList<>();
        StringBuilder currentLine = getCurrentLine();
        if (cursorX >= currentLine.length()) {
            return positions;
        }

        int start = cursorX;
        while (start > 0 && Character.isLetterOrDigit(currentLine.charAt(start - 1))) {
            start--;
        }

        int end = cursorX;
        while (end < currentLine.length() && Character.isLetterOrDigit(currentLine.charAt(end))) {
            end++;
        }

        if (start < end) {
            String word = currentLine.substring(start, end);
            positions = searchWord(word);
        }

        return positions;
    }

    public List<int[]> searchWord(String word) {
        List<int[]> positions = new ArrayList<>();
        for (int y = 0; y < lines.size(); y++) {
            String line = lines.get(y).toString();
            int x = line.indexOf(word);
            while (x != -1) {
                positions.add(new int[]{y, x});
                x = line.indexOf(word, x + 1);
            }
        }
        return positions;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
