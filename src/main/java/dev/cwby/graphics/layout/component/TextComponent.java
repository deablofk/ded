package dev.cwby.graphics.layout.component;

import dev.cwby.Deditor;
import dev.cwby.config.ConfigurationParser;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.treesitter.SyntaxHighlighter;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import io.github.treesitter.jtreesitter.Node;

import java.util.Map;

public class TextComponent implements IComponent {

    private static final Paint textPaint = new Paint().setColor(Deditor.getConfig().treesitter.get("default"));
    private static final Paint numberPaint = new Paint().setColor(ConfigurationParser.hexToInt(Deditor.getConfig().theme.numberColor));
    private static final Paint cursorColor = new Paint().setColor(ConfigurationParser.hexToInt(Deditor.getConfig().cursor.color));

    private TextBuffer buffer;
    private boolean cursorVisible = true;
    private long lastBlinkTime = 0;

    public TextComponent setBuffer(TextBuffer buffer) {
        this.buffer = buffer;
        return this;
    }

    public TextBuffer getBuffer() {
        return buffer;
    }

    private void drawHighlightedText(Canvas canvas, String text, float x, float y, float width, float height, Map<Integer, Paint> styles) {
        float offsetX = x;
        var currentText = new StringBuilder();
        Paint currentPaint = null;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            Paint paint = styles.getOrDefault(i, textPaint);

            if (currentPaint == null || !currentPaint.equals(paint)) {
                if (!currentText.isEmpty()) {
                    Font font = SkiaRenderer.fontManager.resolveFontForGlyph(codePoint);
                    canvas.drawString(currentText.toString(), offsetX, y, font, currentPaint);
                    offsetX += font.measureTextWidth(currentText.toString());
                    currentText.setLength(0);
                }
                currentPaint = paint;
            }

            currentText.append(Character.toChars(codePoint));
            i += Character.charCount(codePoint);
        }
        if (!currentText.isEmpty()) {
            Font font = SkiaRenderer.fontManager.resolveFontForGlyph(currentText.codePointAt(0));
            canvas.drawString(currentText.toString(), offsetX, y, font, currentPaint);
        }
    }

    public void renderText(Canvas canvas, float x, float y, float width, float height, int offsetY) {
        Font font = SkiaRenderer.fontManager.getDefaultFont();
        float lineHeight = SkiaRenderer.fontManager.getLineHeight();
        int startLine = (int) Math.max(0, offsetY / lineHeight);
        int endLine = (int) Math.min(buffer.lines.size(), (double) (offsetY + height) / lineHeight);
        for (int i = startLine; i < endLine; i++) {
            StringBuilder line = buffer.lines.get(i);
            Node root = SyntaxHighlighter.parse(line.toString());
            Map<Integer, Paint> styles = SyntaxHighlighter.highlight(root, line.toString());
//            String number = i + "~ ";
//            float numberWidth = font.measureTextWidth(number);
//            canvas.drawString(number, x, (y + lineHeight) + i * lineHeight, font, numberPaint);
            drawHighlightedText(canvas, line.toString(), x, (y + lineHeight) + i * lineHeight, width, height, styles);
        }
    }

    public void renderCursor(Canvas canvas, float bufferX, float bufferY, FontManager fontManager) {
        long now = System.currentTimeMillis();

        if (now - lastBlinkTime >= Deditor.getConfig().cursor.blink) {
            cursorVisible = !cursorVisible;
            lastBlinkTime = now;
        }

        TextComponent textComponent = (TextComponent) SkiaRenderer.currentNode.component;
        if (textComponent != null && textComponent.getBuffer() != null) {
            TextBuffer buffer = textComponent.getBuffer();
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
                    canvas.drawRect(Rect.makeXYWH(bufferX + x, bufferY + y, fontManager.getAvgWidth(), fontManager.getLineHeight()), cursorColor);
                } else if (Deditor.getBufferMode() == TextInteractionMode.INSERT) {
                    canvas.drawRect(Rect.makeXYWH(bufferX + x, bufferY + y, 2, fontManager.getLineHeight()), cursorColor);
                }
            }
        }
    }

    @Override
    public void render(Canvas canvas, float x, float y, float width, float height) {
        canvas.save();
        canvas.clipRect(Rect.makeXYWH(x, y, x + width, y + height));
        canvas.clear(ConfigurationParser.hexToInt(Deditor.getConfig().theme.background));
        if (buffer != null) {
            renderText(canvas, x, y, width, height, buffer.offsetY);
            if (SkiaRenderer.currentNode.component == this) {
                renderCursor(canvas, x, y, SkiaRenderer.fontManager);
            }
        }
        canvas.restore();
    }
}
