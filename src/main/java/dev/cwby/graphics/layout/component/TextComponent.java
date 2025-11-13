package dev.cwby.graphics.layout.component;

import dev.cwby.Deditor;
import dev.cwby.config.ConfigurationParser;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.OpenGLRenderer;
import dev.cwby.graphics.opengl.GLFont;
import dev.cwby.graphics.opengl.Renderer2D;
import dev.cwby.input.GlobalKeyHandler;
import dev.cwby.treesitter.SyntaxHighlighter;
import io.github.treesitter.jtreesitter.Node;

import java.util.HashMap;
import java.util.Map;

public class TextComponent implements IComponent {

    private static final int textColor = Deditor.getConfig().treesitter.get("default");
    private static final int numberColor = ConfigurationParser.hexToInt(Deditor.getConfig().theme.numberColor);
    private static final int cursorColorInt = ConfigurationParser.hexToInt(Deditor.getConfig().cursor.color);
    private static final int selectColorInt = ConfigurationParser.hexToInt(Deditor.getConfig().cursor.select);
    private static final int lineBackgroundColor = 0x66666666;
    private static final int borderColor = 0xFF000000;

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

    private void drawHighlightedText(Renderer2D renderer, String text, float x, float y, float width, float height, Map<Integer, Integer> styles) {
        float offsetX = x - buffer.offsetX;
        StringBuilder currentText = new StringBuilder();
        int currentColor = textColor;
        GLFont font = FontManager.getDefaultFont();
        float baselineOffset = font.getAscent();
        float textY = y + baselineOffset;
        int tabSize = 4;
        float spaceWidth = font.measureText(" ");

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            int color = styles.getOrDefault(i, textColor);

            if (codePoint == '\t') {
                if (currentText.length() > 0) {
                    renderer.drawText(currentText.toString(), offsetX, textY, font, currentColor);
                    offsetX += font.measureText(currentText.toString());
                    currentText.setLength(0);
                }

                float tabWidth = spaceWidth * tabSize;
                offsetX = ((int) ((offsetX + tabWidth) / tabWidth)) * tabWidth;
            } else {
                if (currentColor != color) {
                    if (currentText.length() > 0) {
                        renderer.drawText(currentText.toString(), offsetX, textY, font, currentColor);
                        offsetX += font.measureText(currentText.toString());
                        currentText.setLength(0);
                    }
                    currentColor = color;
                }
                currentText.append(Character.toChars(codePoint));
            }

            i += Character.charCount(codePoint);
        }
        if (currentText.length() > 0) {
            renderer.drawText(currentText.toString(), offsetX, textY, font, currentColor);
        }
    }

    public void drawCurrentLineBackground(Renderer2D renderer, float x, float y, float width, float lineHeight) {
        float currentLineY = (buffer.cursorY - buffer.offsetY) * lineHeight;
        renderer.drawRect(x, y + currentLineY, width, lineHeight, lineBackgroundColor);
    }

    public void renderText(Renderer2D renderer, float x, float y, float width, float height, int offsetY) {
        float lineHeight = FontManager.getLineHeight();
        for (int i = offsetY, count = 0; i < buffer.lines.size(); i++, count++) {
            StringBuilder line = buffer.lines.get(i);
            Node root = SyntaxHighlighter.parse(line.toString(), buffer.getFileType());
            Map<Integer, Integer> styles = SyntaxHighlighter.highlight(root, line.toString());
            drawHighlightedText(renderer, line.toString(), x, y + count * lineHeight, width, height, styles);
        }
    }

    public void renderCursor(Renderer2D renderer, float bufferX, float bufferY) {
        long now = System.currentTimeMillis();

        if (now - GlobalKeyHandler.lastKeyPressTime >= 5000) {
            if (now - lastBlinkTime >= Deditor.getConfig().cursor.blink) {
                cursorVisible = !cursorVisible;
                lastBlinkTime = now;
            }
        } else {
            cursorVisible = true;
        }

        TextComponent textComponent = (TextComponent) OpenGLRenderer.WM.getCurrentWindow().component;
        if (textComponent != null && textComponent.getBuffer() != null) {
            ScratchBuffer buffer = textComponent.getBuffer();
            int cursorX = buffer.cursorX;
            int cursorY = buffer.cursorY;

            if (cursorVisible && (cursorY < buffer.lines.size())) {
                float x = -buffer.offsetX;
                if (cursorY >= 0) {
                    StringBuilder line = buffer.lines.get(cursorY);
                    int tabSize = 4;
                    float spaceWidth = FontManager.getDefaultFont().measureText(" ");
                    for (int i = 0; i < cursorX && i < line.length(); ) {
                        int codePoint = line.codePointAt(i);
                        if (codePoint == '\t') {
                            float tabWidth = spaceWidth * tabSize;
                            x = ((int) ((x + tabWidth) / tabWidth)) * tabWidth;
                        } else {
                            GLFont font = FontManager.getDefaultFont();
                            String glyph = new String(Character.toChars(codePoint));
                            x += font.measureText(glyph);
                        }
                        i += Character.charCount(codePoint);
                    }
                }

                float y = (cursorY - buffer.offsetY) * FontManager.getLineHeight();
                if (Deditor.getBufferMode() == TextInteractionMode.NAVIGATION || Deditor.getBufferMode() == TextInteractionMode.SELECT) {
                    renderer.drawRect(bufferX + x, bufferY + y, FontManager.getAvgWidth(), FontManager.getLineHeight(), cursorColorInt);
                } else if (Deditor.getBufferMode() == TextInteractionMode.INSERT) {
                    renderer.drawRect(bufferX + x, bufferY + y, 2, FontManager.getLineHeight(), cursorColorInt);
                }
            }
        }
    }

    public void renderSelection(Renderer2D renderer, float bufferX, float bufferY) {
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
                    GLFont font = FontManager.getDefaultFont();
                    String glyph = new String(Character.toChars(codePoint));
                    float charWidth = font.measureText(glyph);

                    if (i < lineStart) {
                        startXOffset += charWidth;
                    }
                    if (i < lineEnd) {
                        endXOffset += charWidth;
                    }

                    i += Character.charCount(codePoint);
                }

                renderer.drawRect(bufferX + startXOffset, bufferY + lineY, endXOffset - startXOffset, FontManager.getLineHeight(), selectColorInt);
            }
        }
    }

    @Override
    public void render(Renderer2D renderer, float x, float y, float width, float height) {
        renderer.pushClip(x, y, width, height);
        renderer.drawRect(x, y, width, height, ConfigurationParser.hexToInt(Deditor.getConfig().theme.background));
        drawCurrentLineBackground(renderer, x, y, width, FontManager.getLineHeight());
        if (buffer != null) {
            renderText(renderer, x, y, width, height, buffer.offsetY);
            if (OpenGLRenderer.WM.getCurrentWindow().component == this) {
                renderCursor(renderer, x, y);
                renderSelection(renderer, x, y);
            }
        }
        renderer.popClip();
        
        // Draw border after clipping is removed so all edges are visible
        renderer.drawRect(x, y, width, height, borderColor, true);
    }
}
