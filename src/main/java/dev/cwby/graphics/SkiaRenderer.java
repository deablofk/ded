package dev.cwby.graphics;

import dev.cwby.BufferManager;
import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.layout.AutoCompleteWindow;
import dev.cwby.graphics.layout.FloatingWindow;
import dev.cwby.graphics.layout.WindowNode;
import dev.cwby.graphics.layout.component.TextComponent;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Rect;
import org.lwjgl.opengl.GL11;

public class SkiaRenderer implements IRender {
    private BackendRenderTarget renderTarget;
    private final DirectContext context;
    private Surface surface;
    private Canvas canvas;

    private final Paint textPaint;

    public static WindowNode rootNode = new WindowNode(0, 0, Engine.getWidth(), Engine.getHeight() - FontManager.getLineHeight(), null);
    public static WindowNode currentNode = rootNode;
    public static AutoCompleteWindow autoCompleteWindow = new AutoCompleteWindow(0, 0, 400, 0);
    public static FloatingWindow floatingWindow = null;

    public SkiaRenderer() {
        context = DirectContext.makeGL();

        textPaint = new Paint().setColor(Deditor.getConfig().treesitter.get("default"));
        rootNode.component = new TextComponent();
        onResize(Engine.getWidth(), Engine.getHeight());
        if (BufferManager.shouldOpenEmptyBuffer) {
            System.out.println("Opening empty buffer");
            currentNode.component = new TextComponent().setBuffer(BufferManager.addEmptyBuffer());
        }
    }

    @Override
    public void onResize(int width, int height) {
        int fbId = GL11.glGetInteger(0x8CA6);

        try {
            surface.close();
            canvas.close();
        } catch (Exception e) {
        }

        renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, fbId, FramebufferFormat.GR_GL_RGBA8);
        surface = Surface.wrapBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.getSRGB());
        canvas = surface.getCanvas();
        rootNode.updateSize(0, 0, width, height - FontManager.getLineHeight());
    }


    public void renderStatusLine(float x, float y, float width, float height) {
        canvas.save();
        canvas.clipRect(Rect.makeXYWH(x, y, x + width, y + height));
        canvas.clear(0xFF000000);
        if (Deditor.getBufferMode() == TextInteractionMode.COMMAND) {
            canvas.drawString(":" + CommandHandler.getBuffer(), 5, y + FontManager.getLineHeight() - 5, FontManager.getDefaultFont(), textPaint);
        } else {
            canvas.drawString(Deditor.getBufferMode().toString(), 5, y + FontManager.getLineHeight() - 5, FontManager.getDefaultFont(), textPaint);
        }
        canvas.restore();
    }


    @Override
    public void render(int width, int height) {
        renderRegion(canvas, rootNode);
        renderStatusLine(0, height - FontManager.getLineHeight(), width, height);
        renderFloatingWindows(canvas);
        renderAutoCompleteWindow(canvas);
        context.flush();
        surface.flushAndSubmit();
    }


    public void renderRegion(Canvas canvas, WindowNode node) {
        if (node.isLeaf()) {
            if (node.component != null) {
                node.component.render(canvas, node.x, node.y, node.width, node.height);
            }
        } else {
            renderRegion(canvas, node.leftChild);
            renderRegion(canvas, node.rightChild);
        }
    }

    public void renderAutoCompleteWindow(Canvas canvas) {
        if (autoCompleteWindow.isVisible()) {
            TextBuffer textBuffer = ((TextComponent) currentNode.component).getBuffer();
            autoCompleteWindow.render(canvas, textBuffer.cursorX, textBuffer.cursorY, rootNode.width, rootNode.height);
        }
    }

    public void renderFloatingWindows(Canvas canvas) {
        if (floatingWindow != null && floatingWindow.isVisible()) {
            floatingWindow.render(canvas, rootNode.x, rootNode.y, rootNode.width, rootNode.height);
        }
    }

    public static void setFloatingWindow(FloatingWindow window) {
        floatingWindow = window;
    }

    public static TextBuffer getCurrentTextBuffer() {
        return ((TextComponent) currentNode.component).getBuffer();
    }
}
