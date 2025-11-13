package dev.cwby.graphics;

import dev.cwby.Deditor;
import dev.cwby.config.data.FontConfig;
import dev.cwby.graphics.opengl.GLFont;

import java.io.IOException;

public class FontManager {

    private static final FontConfig cfg = Deditor.getConfig().font;
    private static GLFont defaultFont;
    private static float lineHeight;
    private static float avgWidth;

    static {
        try {
            initializeFont();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize font", e);
        }
    }

    private static void initializeFont() throws IOException {
        // Try to load the configured font, fallback to system fonts
        String fontPath = findFontPath(cfg.family);
        defaultFont = new GLFont(fontPath, cfg.size);
        lineHeight = defaultFont.getLineHeight();
        avgWidth = defaultFont.measureText("M"); // Use 'M' as average width reference
        System.out.println("Font initialized: size=" + cfg.size + ", lineHeight=" + lineHeight);
    }

    private static String findFontPath(String fontFamily) {
        // Common font paths on Linux
        String[] possiblePaths = {
            "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf",
            "/usr/share/fonts/TTF/DejaVuSansMono.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationMono-Regular.ttf",
            "/usr/share/fonts/liberation/LiberationMono-Regular.ttf",
            "/usr/share/fonts/truetype/ubuntu/UbuntuMono-R.ttf",
            "/usr/share/fonts/ubuntu/UbuntuMono-R.ttf",
            "/usr/share/fonts/truetype/noto/NotoMono-Regular.ttf",
            "/usr/share/fonts/noto/NotoMono-Regular.ttf",
            "/System/Library/Fonts/Menlo.ttc", // macOS
            "C:\\Windows\\Fonts\\consola.ttf"  // Windows
        };

        for (String path : possiblePaths) {
            if (new java.io.File(path).exists()) {
                return path;
            }
        }

        // Fallback to DejaVu Sans Mono (most common on Linux)
        return "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf";
    }

    public static float getLineHeight() {
        return lineHeight;
    }

    public static float getAvgWidth() {
        return avgWidth;
    }

    public static GLFont getDefaultFont() {
        return defaultFont;
    }

    public static void increaseFontSize(int sizeIncrease) {
        cfg.increaseFontSize(sizeIncrease);
        System.out.println("Font size changed: " + cfg.size + " (change: " + (sizeIncrease > 0 ? "+" : "") + sizeIncrease + ")");
        try {
            // Clean up old font
            if (defaultFont != null) {
                defaultFont.cleanup();
            }
            // Reinitialize with new size
            initializeFont();
        } catch (IOException e) {
            System.err.println("Failed to update font size: " + e.getMessage());
        }
    }

    public static float measureText(String text) {
        return defaultFont.measureText(text);
    }
}
