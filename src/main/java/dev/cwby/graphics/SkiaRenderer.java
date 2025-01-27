package dev.cwby.graphics;

import dev.cwby.Deditor;
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

    private static boolean cursorVisible = true;
    private static long lastBlinkTime = 0;
    private static final int BLINK_INTERVAL = 500; // 500ms
    private static float cursorWidth;
    private static final Paint cursorColor = new Paint().setColor(0xFF888888);

    public SkiaRenderer() {
        context = DirectContext.makeGL();
        onResize(1280, 720);
        textPaint = new Paint().setColor(0xFFFFFFFF);
        this.fallbackTypefaces.add(Typeface.makeFromName("Iosevka Nerd Font", FontStyle.NORMAL));
        this.fallbackTypefaces.addAll(getAllSystemFonts());
        this.font = new Font(fallbackTypefaces.getFirst(), 24);
        lineHeight = font.getMetrics().getHeight();
        cursorWidth = font.getMetrics().getAvgCharWidth();
    }

    public List<Typeface> getAllSystemFonts() {
        var fontDir = Paths.get("/usr/share/fonts");
        List<Typeface> fonts = new ArrayList<>();

        try {
            Files.walkFileTree(fontDir, new SimpleFileVisitor<java.nio.file.Path>() {
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

    private final Map<Integer, Font> codePointFontCache = new HashMap<>();

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
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            Paint paint = styles.getOrDefault(i, textPaint);
            Font font = resolveFontForGlyph(codePoint);

            String glyph = new String(Character.toChars(codePoint));
            canvas.drawString(glyph, offsetX, y, font, paint);

            offsetX += font.measureTextWidth(glyph);
            i += Character.charCount(codePoint);
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

        if (now - lastBlinkTime >= BLINK_INTERVAL) {
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
            if (Deditor.getMode() == TextInteractionMode.NAVIGATION) {
                canvas.drawRect(Rect.makeXYWH(x, y, cursorWidth, lineHeight), cursorColor);
            } else if (Deditor.getMode() == TextInteractionMode.INSERT) {
                canvas.drawRect(Rect.makeXYWH(x, y, 2, lineHeight), cursorColor);
            }
        }
    }

    public void renderText() {
        for (int i = 0; i < Deditor.buffer.lines.size(); i++) {
            StringBuilder line = Deditor.buffer.lines.get(i);
            Node root = SyntaxHighlighter.parse(line.toString());
            Map<Integer, Paint> styles = SyntaxHighlighter.highlight(root, line.toString());
            drawHighlightedText(line.toString(), 0, 24 + i * lineHeight, styles);
        }
    }

    public void renderBackground() {
        canvas.clear(0xFF000000);
    }

    public void renderStatusLine(float posY, float drawDuration) {
        if (Deditor.getMode() == TextInteractionMode.COMMAND) {
            drawStringWithFontFallback(":" + Deditor.commandBuffer.toString(), 0, posY, textPaint);
        } else {
            drawStringWithFontFallback(Deditor.getMode() + " | drawDuration(" + drawDuration + "ms)", 0, posY, textPaint);
        }
    }

    @Override
    public void render(int width, int height) {
        long startTime = System.nanoTime();
        renderBackground();
        renderText();
        renderCursor();

        long endTime = System.nanoTime();
        float duration = (float) ((endTime - startTime) / 1_000_000.0);
        renderStatusLine(height - lineHeight, duration);
        context.flush();
        surface.flushAndSubmit();
    }
}
