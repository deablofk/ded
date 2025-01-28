package dev.cwby.config.data;

import java.util.Map;

public class EditorConfig {

    public Font font;
    public Cursor cursor;
    public Map<String, Integer> treesitter;
    public Theme theme;

    public EditorConfig(Cursor cursor, Font font, Theme theme, Map<String, Integer> treesitter) {
        this.cursor = cursor;
        this.font = font;
        this.theme = theme;
        this.treesitter = treesitter;
    }

}