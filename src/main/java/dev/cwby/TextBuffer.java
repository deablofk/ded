package dev.cwby;

import java.util.ArrayList;
import java.util.List;

public class TextBuffer {
    private final List<StringBuilder> lines = new ArrayList<>();
    private int cursorX = 0;
    private int cursorY = 0;

    public TextBuffer() {
        lines.add(new StringBuilder("1- Hello From IOSEVKA!!!"));
        lines.add(new StringBuilder("2- Hello From IOSEVKA!!!"));
        lines.add(new StringBuilder("3- Hello From IOSEVKA!!!"));
        lines.add(new StringBuilder("4- Hello From IOSEVKA!!!"));
        lines.add(new StringBuilder("5- Hello From IOSEVKA!!!"));
        lines.add(new StringBuilder("6- Hello From IOSEVKA!!!"));
    }

    public List<StringBuilder> getLines() {
        return lines;
    }
}
