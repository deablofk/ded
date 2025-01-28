package dev.cwby.graphics;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.config.ConfigurationParser;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.treesitter.SyntaxHighlighter;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Rect;
import io.github.treesitter.jtreesitter.Node;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkiaRenderer implements IRender {
    private BackendRenderTarget renderTarget;
    private final DirectContext context;
    private Surface surface;
    private Canvas canvas;

    private final Paint textPaint;
    private final float lineHeight;

    private final List<Typeface> fallbackTypefaces = new ArrayList<>();

    private final Font font;

    private boolean cursorVisible = true;
    private long lastBlinkTime = 0;
    private float cursorWidth;
    private final Paint cursorColor = new Paint().setColor(ConfigurationParser.hexToInt(Deditor.config.cursor.color));
    private final Map<Integer, Font> codePointFontCache = new HashMap<>();

    public SkiaRenderer() {
        context = DirectContext.makeGL();
        onResize(1280, 720);
        textPaint = new Paint().setColor(Deditor.config.treesitter.get("default"));
        var fontConfig = Deditor.config.font;
        this.fallbackTypefaces.add(Typeface.makeFromName(fontConfig.family, FontStyle.NORMAL));
        this.fallbackTypefaces.addAll(getAllSystemFonts());
        this.font = new Font(fallbackTypefaces.getFirst(), fontConfig.size);
        lineHeight = font.getMetrics().getHeight();
        cursorWidth = font.getMetrics().getAvgCharWidth();
    }

    public List<Typeface> getAllSystemFonts() {
        var fontDir = Paths.get("/usr/share/fonts");
        List<Typeface> fonts = new ArrayList<>();

        try {
            Files.walkFileTree(fontDir, new SimpleFileVisitor<>() {
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


    private Font resolveFontForGlyph(int codePoint) {
        if (codePointFontCache.containsKey(codePoint)) {
            return codePointFontCache.get(codePoint);
        }

        for (Typeface typeface : fallbackTypefaces) {
            Font font = new Font(typeface, 24);
            if (font.getUTF32Glyph(codePoint) != 0) {
                codePointFontCache.put(codePoint, font);
                return font;
            }
        }
        return font;
    }

    public void drawStringWithFontFallback(String text, float x, float y, Paint paint) {
        float offsetX = x;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            Font font = resolveFontForGlyph(codePoint);

            String glyph = new String(Character.toChars(codePoint));
            canvas.drawString(glyph, offsetX, y, font, paint);

            offsetX += font.measureTextWidth(glyph);
            i += Character.charCount(codePoint);
        }
    }

    private void drawHighlightedText(String text, float x, float y, Map<Integer, Paint> styles) {
        float offsetX = x;
        var currentText = new StringBuilder();
        Paint currentPaint = null;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            Paint paint = styles.getOrDefault(i, textPaint);

            if (currentPaint == null || !currentPaint.equals(paint)) {
                if (!currentText.isEmpty()) {
                    Font font = resolveFontForGlyph(codePoint);
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
            Font font = resolveFontForGlyph(currentText.codePointAt(0));
            canvas.drawString(currentText.toString(), offsetX, y, font, currentPaint);
        }
    }

    @Override
    public void onResize(int width, int height) {
        int fbId = GL11.glGetInteger(0x8CA6);
        renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, fbId, FramebufferFormat.GR_GL_RGBA8);
        surface = Surface.wrapBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.getSRGB());
        canvas = surface.getCanvas();
    }

    public void renderCursor() {
        long now = System.currentTimeMillis();

        if (now - lastBlinkTime >= Deditor.config.cursor.blink) {
            cursorVisible = !cursorVisible;
            lastBlinkTime = now;
        }

        int cursorX = Deditor.buffer.cursorX;
        int cursorY = Deditor.buffer.cursorY;

        if (cursorVisible && (cursorY < Deditor.buffer.lines.size())) {
            float x = 0;
            if (cursorY >= 0) {
                StringBuilder line = Deditor.buffer.lines.get(cursorY);
                for (int i = 0; i < cursorX && i < line.length(); ) {
                    int codePoint = line.codePointAt(i);
                    Font font = resolveFontForGlyph(codePoint);
                    String glyph = new String(Character.toChars(codePoint));
                    x += font.measureTextWidth(glyph);
                    i += Character.charCount(codePoint);
                }
            }

            float y = cursorY * lineHeight;
            if (Deditor.getBufferMode() == TextInteractionMode.NAVIGATION) {
                canvas.drawRect(Rect.makeXYWH(x, y, cursorWidth, lineHeight), cursorColor);
            } else if (Deditor.getBufferMode() == TextInteractionMode.INSERT) {
                canvas.drawRect(Rect.makeXYWH(x, y, 2, lineHeight), cursorColor);
            }
        }
    }

    public void renderText(int offsetY, int viewportHeight) {
        int startLine = (int) Math.max(0, offsetY / lineHeight);
        int endLine = (int) Math.min(Deditor.buffer.lines.size(), (double) (offsetY + viewportHeight) / lineHeight);
        for (int i = startLine; i < endLine; i++) {
            StringBuilder line = Deditor.buffer.lines.get(i);
            Node root = SyntaxHighlighter.parse(line.toString());
            Map<Integer, Paint> styles = SyntaxHighlighter.highlight(root, line.toString());
            drawHighlightedText(line.toString(), 0, 24 + i * lineHeight, styles);
        }
    }

    public void renderBackground() {
        canvas.clear(ConfigurationParser.hexToInt(Deditor.config.theme.background));
    }

    public void renderStatusLine(float posY, float drawDuration) {
        if (Deditor.getBufferMode() == TextInteractionMode.COMMAND) {
            drawStringWithFontFallback(":" + CommandHandler.getBuffer(), 0, posY, textPaint);
        } else {
            drawStringWithFontFallback(Deditor.getBufferMode() + " | drawDuration(" + drawDuration + "ms)", 0, posY, textPaint);
        }
    }

    @Override
    public void render(int width, int height) {
        renderBackground();
        renderText(Deditor.buffer.offsetY, height);
        renderCursor();
        renderStatusLine(height - lineHeight, 0);
        context.flush();
        surface.flushAndSubmit();
    }
}
