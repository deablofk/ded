package dev.cwby.config;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationParser {

    public static EditorConfig readConfiguration(String path) {
        var file = new File(path);

        if (file.exists()) {
            Toml toml = new Toml().read(file);
            FontConfig font = toml.getTable("font").to(FontConfig.class);
            Cursor cursor = toml.getTable("cursor").to(Cursor.class);
            Map<String, Integer> theme = parseTheme(toml.getTable("theme"));
            return new EditorConfig(cursor, font, theme);
        }
        return null;
    }

    private static Map<String, Integer> parseTheme(Toml themeTable) {
        Map<String, Integer> theme = new HashMap<>();
        for (Map.Entry<String, Object> entry : themeTable.toMap().entrySet()) {
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue()).replace("#", "");
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
                theme.put(key.translateEscapes(), Integer.parseInt(value, 16) | 0xFF000000);
            } else {
                theme.put(key.translateEscapes(), Integer.parseInt(value, 16) | 0xFF000000);
            }
        }
        return theme;
    }

}
