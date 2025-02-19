package dev.cwby.graphics.layout.component;

import dev.cwby.Deditor;
import dev.cwby.config.ConfigurationParser;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.input.GlobalKeyHandler;
import dev.cwby.treesitter.SyntaxHighlighter;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import io.github.treesitter.jtreesitter.Node;

import java.util.Map;


public class TextComponent implements IComponent {

    private static final Paint textPaint = new Paint().setColor(Deditor.getConfig().treesitter.get("default"));
    private static final Paint numberPaint = new Paint().setColor(ConfigurationParser.hexToInt(Deditor.getConfig().theme.numberColor));
    private static final Paint cursorColor = new Paint().setColor(ConfigurationParser.hexToInt(Deditor.getConfig().cursor.color));
    private static final Paint selectColor = new Paint().setColor(ConfigurationParser.hexToInt(Deditor.getConfig().cursor.select));

    private ScratchBuffer buffer;
    private boolean cursorVisible = true;
    private long lastBlinkTime = 0;

    public TextComponent setBuffer(ScratchBuffer buffer) {
        this.buffer = buffer;
        return this;
    }

    public ScratchBuffer getBuffer() {
        return buffer;
    }

    private void drawHighlightedText(Canvas canvas, String text, float x, float y, float width, float height, Map<Integer, Paint> styles) {
        float offsetX = x - buffer.offsetX;
        var currentText = new StringBuilder();
        Paint currentPaint = null;
        Font font = FontManager.getDefaultFont();
        FontMetrics metrics = font.getMetrics();
        float baselineOffset = -metrics.getAscent();
        float textY = y + baselineOffset;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            Paint paint = styles.getOrDefault(i, textPaint);

            if (currentPaint == null || !currentPaint.equals(paint)) {
                if (!currentText.isEmpty()) {
                    canvas.drawString(currentText.toString(), offsetX, textY, font, currentPaint);
                    offsetX += font.measureTextWidth(currentText.toString());
                    currentText.setLength(0);
                }
                currentPaint = paint;
            }

            currentText.append(Character.toChars(codePoint));
            i += Character.charCount(codePoint);
        }
        if (!currentText.isEmpty()) {
            canvas.drawString(currentText.toString(), offsetX, textY, font, currentPaint);
        }
    }

    Paint lineBackgroundPaint = new Paint().setColor(0x66666666);

    public void drawCurrentLineBackground(Canvas canvas, float x, float y, float width, float lineHeight) {
        float currentLineY = (buffer.cursorY - buffer.offsetY) * lineHeight;
        canvas.drawRect(Rect.makeXYWH(x, y + currentLineY, width, lineHeight), lineBackgroundPaint);
    }

    public void renderText(Canvas canvas, float x, float y, float width, float height, int offsetY) {
        float lineHeight = FontManager.getLineHeight();
        for (int i = offsetY, count = 0; i < buffer.lines.size(); i++, count++) {
            StringBuilder line = buffer.lines.get(i);
            Node root = SyntaxHighlighter.parse(line.toString(), buffer.getFileType());
            Map<Integer, Paint> styles = SyntaxHighlighter.highlight(root, line.toString());
            drawHighlightedText(canvas, line.toString(), x, y + count * lineHeight, width, height, styles);
        }
    }

    public void renderCursor(Canvas canvas, float bufferX, float bufferY) {
        long now = System.currentTimeMillis();

        if (now - GlobalKeyHandler.lastKeyPressTime >= 5000) {
            if (now - lastBlinkTime >= Deditor.getConfig().cursor.blink) {
                cursorVisible = !cursorVisible;
                lastBlinkTime = now;
            }
        } else {
            cursorVisible = true;
        }

        TextComponent textComponent = (TextComponent) SkiaRenderer.WM.getCurrentWindow().component;
        if (textComponent != null && textComponent.getBuffer() != null) {
            ScratchBuffer buffer = textComponent.getBuffer();
            int cursorX = buffer.cursorX;
            int cursorY = buffer.cursorY;

            if (cursorVisible && (cursorY < buffer.lines.size())) {
                float x = -buffer.offsetX;
                if (cursorY >= 0) {
                    StringBuilder line = buffer.lines.get(cursorY);
                    for (int i = 0; i < cursorX && i < line.length(); ) {
                        int codePoint = line.codePointAt(i);
                        Font font = FontManager.getDefaultFont();
                        String glyph = new String(Character.toChars(codePoint));
                        x += font.measureTextWidth(glyph);
                        i += Character.charCount(codePoint);
                    }
                }

                float y = (cursorY - buffer.offsetY) * FontManager.getLineHeight();
                if (Deditor.getBufferMode() == TextInteractionMode.NAVIGATION || Deditor.getBufferMode() == TextInteractionMode.SELECT) {
                    canvas.drawRect(Rect.makeXYWH(bufferX + x, bufferY + y, FontManager.getAvgWidth(), FontManager.getLineHeight()), cursorColor);
                } else if (Deditor.getBufferMode() == TextInteractionMode.INSERT) {
                    canvas.drawRect(Rect.makeXYWH(bufferX + x, bufferY + y, 2, FontManager.getLineHeight()), cursorColor);
                }
            }
        }
    }

    public void renderSelection(Canvas canvas, float bufferX, float bufferY) {
        int cursorX = buffer.cursorX;
        int cursorY = buffer.cursorY;
        int selectX = GlobalKeyHandler.startVisualX;
        int selectY = GlobalKeyHandler.startVisualY;
        if (Deditor.getBufferMode() == TextInteractionMode.SELECT) {
            int startLine = Math.min(cursorY, selectY);
            int endLine = Math.max(cursorY, selectY);
            int startChar = (cursorY < selectY) ? cursorX : selectX;
            int endChar = (cursorY > selectY) ? cursorX : selectX;

            for (int line = startLine; line <= endLine; line++) {
                float lineY = (line - buffer.offsetY) * FontManager.getLineHeight();
                StringBuilder lineContent = buffer.lines.get(line);

                int lineStart = (line == startLine) ? startChar : 0;
                int lineEnd = (line == endLine) ? endChar : lineContent.length();
                float startXOffset = 0;
                float endXOffset = 0;

                for (int i = 0; i < lineContent.length(); ) {
                    int codePoint = lineContent.codePointAt(i);
                    Font font = FontManager.getDefaultFont();
                    String glyph = new String(Character.toChars(codePoint));
                    float charWidth = font.measureTextWidth(glyph);

                    if (i < lineStart) {
                        startXOffset += charWidth;
                    }
                    if (i < lineEnd) {
                        endXOffset += charWidth;
                    }

                    i += Character.charCount(codePoint);
                }

                canvas.drawRect(Rect.makeXYWH(bufferX + startXOffset, bufferY + lineY, endXOffset - startXOffset, FontManager.getLineHeight()), selectColor);
            }
        }
    }


    private final Paint borderPaint = new Paint().setColor(0xFF000000).setStroke(true).setStrokeWidth(5);

    @Override
    public void render(Canvas canvas, float x, float y, float width, float height) {
        canvas.save();
        Rect rect = Rect.makeXYWH(x, y, width, height);
        canvas.clipRect(rect);
        canvas.clear(ConfigurationParser.hexToInt(Deditor.getConfig().theme.background));
        drawCurrentLineBackground(canvas, x, y, x + width, FontManager.getLineHeight());
        canvas.drawRect(rect, borderPaint);
        if (buffer != null) {
            renderText(canvas, x, y, width, height, buffer.offsetY);
            if (SkiaRenderer.WM.getCurrentWindow().component == this) {
                renderCursor(canvas, x, y);
                renderSelection(canvas, x, y);
            }
        }
        canvas.restore();
    }
}
