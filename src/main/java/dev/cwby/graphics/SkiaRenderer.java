package dev.cwby.graphics;

import dev.cwby.BufferManager;
import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.layout.RegionNode;
import dev.cwby.graphics.layout.component.TextComponent;
import io.github.humbleui.skija.*;
import org.lwjgl.opengl.GL11;

public class SkiaRenderer implements IRender {
    private BackendRenderTarget renderTarget;
    private final DirectContext context;
    private Surface surface;
    private Canvas canvas;

    private final Paint textPaint;
    public static final FontManager fontManager = new FontManager();

    public static RegionNode rootNode = new RegionNode(0, 0, 1280, 720);
    public static RegionNode currentNode = rootNode;

    public SkiaRenderer() {
        context = DirectContext.makeGL();
        textPaint = new Paint().setColor(Deditor.getConfig().treesitter.get("default"));
        rootNode.component = new TextComponent();
        onResize(1280, 720);
        if (BufferManager.shouldOpenEmptyBuffer) {
            System.out.println("Opening empty buffer");
            currentNode.component = new TextComponent().setBuffer(BufferManager.addEmptyBuffer());
        }
    }

    public void drawStringWithFontFallback(String text, float x, float y, Paint paint) {
        float offsetX = x;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            Font font = fontManager.resolveFontForGlyph(codePoint);

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
        rootNode.updateSize(0, 0, width, height);
    }


    public void renderStatusLine(float posY, float drawDuration) {
        if (Deditor.getBufferMode() == TextInteractionMode.COMMAND) {
            drawStringWithFontFallback(":" + CommandHandler.getBuffer(), 0, posY, textPaint);
//            canvas.drawString(":" + CommandHandler.getBuffer(), 0, posY, fontManager.getDefaultFont(), textPaint);
        } else {
            drawStringWithFontFallback(Deditor.getBufferMode() + "|" + CommandHandler.getBuffer(), 0, posY, textPaint);
        }
    }


    @Override
    public void render(int width, int height) {
        renderRegion(canvas, rootNode);
        renderStatusLine(height - fontManager.getLineHeight(), 0);
        context.flush();
        surface.flushAndSubmit();
    }


    public void renderRegion(Canvas canvas, RegionNode node) {
        if (node.isLeaf()) {
            if (node.component != null) {
                node.component.render(canvas, node.x, node.y, node.width, node.height);
            }
        } else {
            renderRegion(canvas, node.leftChild);
            renderRegion(canvas, node.rightChild);
        }
    }

}
