package dev.cwby.graphics.editor;

import dev.cwby.BufferManager;
import dev.cwby.Deditor;
import dev.cwby.config.ConfigurationParser;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.FontManager;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;

import java.nio.Buffer;

public class CursorRender {

    private Canvas canvas;
    private boolean cursorVisible = true;
    private long lastBlinkTime = 0;
    private final Paint cursorColor;
    private final FontManager fontManager;

    public CursorRender(Canvas canvas, FontManager fontManager) {
        cursorColor = new Paint().setColor(ConfigurationParser.hexToInt(Deditor.config.cursor.color));
        this.canvas = canvas;
        this.fontManager = fontManager;
    }

    public void updateCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void render() {
        long now = System.currentTimeMillis();

        if (now - lastBlinkTime >= Deditor.config.cursor.blink) {
            cursorVisible = !cursorVisible;
            lastBlinkTime = now;
        }

        TextBuffer buffer = BufferManager.getActualBuffer();
        int cursorX = buffer.cursorX;
        int cursorY = buffer.cursorY;

        if (cursorVisible && (cursorY < buffer.lines.size())) {
            float x = 0;
            if (cursorY >= 0) {
                StringBuilder line = buffer.lines.get(cursorY);
                for (int i = 0; i < cursorX && i < line.length(); ) {
                    int codePoint = line.codePointAt(i);
                    Font font = fontManager.resolveFontForGlyph(codePoint);
                    String glyph = new String(Character.toChars(codePoint));
                    x += font.measureTextWidth(glyph);
                    i += Character.charCount(codePoint);
                }
            }


            float y = cursorY * fontManager.getLineHeight();
            if (Deditor.getBufferMode() == TextInteractionMode.NAVIGATION) {
                canvas.drawRect(Rect.makeXYWH(x, y, fontManager.getAvgWidth(), fontManager.getLineHeight()), cursorColor);
            } else if (Deditor.getBufferMode() == TextInteractionMode.INSERT) {
                canvas.drawRect(Rect.makeXYWH(x, y, 2, fontManager.getLineHeight()), cursorColor);
            }
        }
    }

}
