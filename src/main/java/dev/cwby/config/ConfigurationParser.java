package dev.cwby.config;

import com.moandjiezana.toml.Toml;
import dev.cwby.config.data.Cursor;
import dev.cwby.config.data.EditorConfig;
import dev.cwby.config.data.FontConfig;
import dev.cwby.config.data.Theme;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigurationParser {

    public static Optional<EditorConfig> read(String path) {
        var file = new File(path);

        if (file.exists()) {
            Toml toml = new Toml().read(file);
            FontConfig font = toml.getTable("font").to(FontConfig.class);
            Cursor cursor = toml.getTable("cursor").to(Cursor.class);
            Theme theme = toml.getTable("theme").to(Theme.class);
            Map<String, Integer> treesitter = parseTheme(toml.getTable("treesitter"));
            return Optional.of(new EditorConfig(cursor, font, theme, treesitter));
        }
        return Optional.empty();
    }

    private static Map<String, Integer> parseTheme(Toml themeTable) {
        Map<String, Integer> theme = new HashMap<>();
        for (Map.Entry<String, Object> entry : themeTable.toMap().entrySet()) {
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue());
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
                theme.put(key.translateEscapes(), hexToInt(value));
            } else {
                theme.put(key.translateEscapes(), hexToInt(value));
            }
        }
        return theme;
    }


    public static int hexToInt(String hexColor) {
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }

        if (hexColor.length() == 8) {
            return (int) Long.parseLong(hexColor, 16);
        } else if (hexColor.length() == 6) {
            return (int) Long.parseLong(hexColor, 16) | 0xFF000000;
        } else {
            throw new IllegalArgumentException("Invalid hex color format. Must be 6 or 8 characters.");
        }
    }
}
