package dev.cwby.graphics.layout.component;

import dev.cwby.Deditor;
import dev.cwby.config.ConfigurationParser;
import dev.cwby.editor.TextBuffer;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.treesitter.SyntaxHighlighter;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import io.github.treesitter.jtreesitter.Node;

import java.util.Map;

public class TextComponent implements IComponent {

    private static final Paint textPaint = new Paint().setColor(Deditor.config.treesitter.get("default"));

    private TextBuffer buffer;

    public TextComponent setBuffer(TextBuffer buffer) {
        this.buffer = buffer;
        return this;
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

    public void renderText(Canvas canvas, float x, float y, float width, float viewportHeight, int offsetY) {
        int startLine = (int) Math.max(0, offsetY / SkiaRenderer.fontManager.getLineHeight());
        int endLine = (int) Math.min(buffer.lines.size(), (double) (offsetY + viewportHeight) / SkiaRenderer.fontManager.getLineHeight());
        for (int i = startLine; i < endLine; i++) {
            StringBuilder line = buffer.lines.get(i);
            Node root = SyntaxHighlighter.parse(line.toString());
            Map<Integer, Paint> styles = SyntaxHighlighter.highlight(root, line.toString());
            drawHighlightedText(canvas, line.toString(), x, 24 + i * SkiaRenderer.fontManager.getLineHeight(), width, viewportHeight, styles);
        }
    }

    @Override
    public void render(Canvas canvas, float x, float y, float width, float height) {
//        canvas.clear(ConfigurationParser.hexToInt(Deditor.config.theme.background));
        if (SkiaRenderer.currentNode != null && SkiaRenderer.currentNode.component == this) {
            canvas.clear(ConfigurationParser.hexToInt(Deditor.config.theme.background));
        }
        canvas.save();
        canvas.clipRect(Rect.makeXYWH(x, y, x + width, y + height));
        if (buffer != null) {
            renderText(canvas, x, y, width, height, buffer.offsetY);
        }
        canvas.restore();
    }
}
