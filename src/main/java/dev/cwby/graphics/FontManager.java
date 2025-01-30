package dev.cwby.graphics;

import dev.cwby.Deditor;
import dev.cwby.config.data.FontConfig;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontStyle;
import io.github.humbleui.skija.Typeface;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontManager {

    private final FontConfig cfg;
    private final Font defaultFont;
    private final List<Typeface> fallbackTypefaces;
    private final Map<Integer, Font> fontCache;
    private final float lineHeight;
    private final float avgWidth;

    public FontManager() {
        cfg = Deditor.getConfig().font;
        fallbackTypefaces = getSystemFonts();
        fontCache = new HashMap<>();
        defaultFont = new Font(Typeface.makeFromName(cfg.family, FontStyle.NORMAL), cfg.size);
        lineHeight = defaultFont.getMetrics().getHeight();
        avgWidth = defaultFont.getMetrics().getAvgCharWidth();
    }

    public List<Typeface> getSystemFonts() {
        var dir = Paths.get("/usr/share/fonts");
        List<Typeface> fonts = new ArrayList<>();
//
//        try (Stream<Path> paths = Files.walk(dir)) {
//            paths.filter(Files::isRegularFile)
//                    .filter(x -> x.getFileName().endsWith(".ttf"))
//                    .forEach(x -> fonts.add(Typeface.makeFromFile(x.toString())));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.toString().toLowerCase();
                    if (fileName.endsWith(".ttf")) {
                        Typeface font = Typeface.makeFromFile(file.toString());
                        fonts.add(font);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fonts;
    }


    public Font resolveFontForGlyph(int codePoint) {
        if (fontCache.containsKey(codePoint)) {
            return fontCache.get(codePoint);
        } else if (defaultFont.getUTF32Glyph(codePoint) != 0) {
            fontCache.put(codePoint, defaultFont);
            return defaultFont;
        } else {
            for (Typeface typeface : fallbackTypefaces) {
                Font font = new Font(typeface, cfg.size);
                if (font.getUTF32Glyph(codePoint) != 0) {
                    fontCache.put(codePoint, font);
                    return font;
                }
            }

            return defaultFont;
        }
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public float getAvgWidth() {
        return avgWidth;
    }

    public Font getDefaultFont() {
        return defaultFont;
    }
}
