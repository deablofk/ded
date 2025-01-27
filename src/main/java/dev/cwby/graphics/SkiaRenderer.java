package dev.cwby.graphics;

import dev.cwby.Deditor;
import dev.cwby.editor.TextInteractionMode;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Rect;
import org.lwjgl.opengl.GL11;

public class SkiaRenderer implements IRender {
    private BackendRenderTarget renderTarget;
    private final DirectContext context;
    private Surface surface;
    private Canvas canvas;

    private final Paint textPaint;
    private final float lineHeight;

    private final Typeface[] fallbackTypefaces = new Typeface[]{Typeface.makeFromName("Iosevka Nerd Font", FontStyle.NORMAL), Typeface.makeFromName("Noto Color Emoji", FontStyle.NORMAL), Typeface.makeDefault()};
    private final Font font = new Font(fallbackTypefaces[0], 24);

    private static boolean cursorVisible = true;
    private static long lastBlinkTime = 0;
    private static final int BLINK_INTERVAL = 500; // 500ms
    private static float cursorWidth;
    private static final Paint cursorColor = new Paint().setColor(0xFF888888);

    public SkiaRenderer() {
        context = DirectContext.makeGL();
        onResize(1280, 720);
        textPaint = new Paint().setColor(0xFFFFFFFF);
        lineHeight = font.getMetrics().getHeight();
        cursorWidth = font.getMetrics().getAvgCharWidth();
    }

    private Font resolveFontForGlyph(int codePoint) {
        for (Typeface typeface : fallbackTypefaces) {
            Font font = new Font(typeface, 24);
            if (font.getUTF32Glyph(codePoint) != 0) {
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

        if (cursorVisible) {
            float x = 0;
            int cursorX = Deditor.buffer.cursorX;
            int cursorY = Deditor.buffer.cursorY;

            if (cursorY >= 0 && cursorY < Deditor.buffer.lines.size()) {
                StringBuilder line = Deditor.buffer.lines.get(cursorY);

                for (int i = 0; i < cursorX && i < line.length(); ) {
                    int codePoint = line.codePointAt(i);
                    Font font = resolveFontForGlyph(codePoint);
                    String glyph = new String(Character.toChars(codePoint));
                    x += font.measureTextWidth(glyph);
                    i += Character.charCount(codePoint);
                }
            }

            if (x < 0) {
                x = 0;
            }

            float y = cursorY * lineHeight;

            canvas.drawRect(Rect.makeXYWH(x, y, cursorWidth, lineHeight), cursorColor);
        }
    }

    public void renderText() {
        for (int i = 0; i < Deditor.buffer.lines.size(); i++) {
            StringBuilder line = Deditor.buffer.lines.get(i);
            drawStringWithFontFallback(line.toString(), 0, 24 + i * lineHeight, textPaint);
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
