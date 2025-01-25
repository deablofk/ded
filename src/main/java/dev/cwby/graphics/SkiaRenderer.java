package dev.cwby.graphics;

import dev.cwby.TextBuffer;
import dev.cwby.input.GlobalKeyHandler;
import io.github.humbleui.skija.*;
import org.lwjgl.opengl.GL11;

public class SkiaRenderer implements IRender {
    private BackendRenderTarget renderTarget;
    private final DirectContext context;
    private Surface surface;
    private Canvas canvas;
    private final TextBuffer textBuffer;

    private final Paint paint;
    private final Font font;
    private final Paint textPaint;

    private final float lineHeight;

    public SkiaRenderer() {
        context = DirectContext.makeGL();
        onResize(1280, 720);
        textBuffer = new TextBuffer();

        font = new Font(Typeface.makeFromName("Iosevka Nerd Font", FontStyle.NORMAL), 24);
        paint = new Paint().setColor(0xFF00FF00);
        textPaint = new Paint().setColor(0xFFFFFFFF);
        lineHeight = font.getMetrics().getHeight();
    }

    @Override
    public void onResize(int width, int height) {
        int fbId = GL11.glGetInteger(0x8CA6);
        renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, fbId, FramebufferFormat.GR_GL_RGBA8);
        surface = Surface.wrapBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.getSRGB());
        canvas = surface.getCanvas();
    }

    public void render(int width, int height) {
        canvas.clear(0xFF000000);
        canvas.drawString("� DEBUG: width(" + width + ") height(" + height + ")", 0, 24, font, textPaint);
        for (int i = 0; i < textBuffer.getLines().size(); i++) {
            canvas.drawString(textBuffer.getLines().get(i).toString(), 0, 24 + 24 + i * lineHeight, font, textPaint);
        }

        canvas.drawString("STATUS LINE | " + GlobalKeyHandler.MODE, 0, height - lineHeight, font, textPaint);

        context.flush();
        surface.flushAndSubmit();
    }

}
