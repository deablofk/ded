package dev.cwby.config;

import java.util.Map;

public class EditorConfig {

    public FontConfig font;
    public Cursor cursor;
    public Map<String, Integer> theme;

    public EditorConfig(Cursor cursor, FontConfig font, Map<String, Integer> theme) {
        this.cursor = cursor;
        this.font = font;
        this.theme = theme;
    }
}