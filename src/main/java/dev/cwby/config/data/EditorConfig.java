package dev.cwby.config.data;

import java.util.Map;

public class EditorConfig {

    public FontConfig font;
    public Cursor cursor;
    public Map<String, Integer> treesitter;
    public Theme theme;

    public EditorConfig(Cursor cursor, FontConfig font, Theme theme, Map<String, Integer> treesitter) {
        this.cursor = cursor;
        this.font = font;
        this.theme = theme;
        this.treesitter = treesitter;
    }

}