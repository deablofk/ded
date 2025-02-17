package dev.cwby.graphics;

import dev.cwby.Deditor;
import dev.cwby.config.data.FontConfig;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.FontStyle;
import io.github.humbleui.skija.Typeface;

public class FontManager {

    private static final FontConfig cfg = Deditor.getConfig().font;
    private static final Typeface bestTypeface = FontMgr.getDefault().matchFamilyStyle(cfg.family, FontStyle.NORMAL);
    private static final Font bestFont = new Font(bestTypeface, cfg.size);

    public static float getLineHeight() {
        return bestFont.getMetrics().getHeight();
    }

    public static float getAvgWidth() {
        return bestFont.getMetrics().getAvgCharWidth();
    }

    public static Font getDefaultFont() {
        return bestFont;
    }

    public static void increaseFontSize(int size) {
        cfg.increaseFontSize(size);
        bestFont.setSize(cfg.size);
    }
}
