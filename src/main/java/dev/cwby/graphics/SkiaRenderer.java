package dev.cwby.graphics;

import dev.cwby.BufferManager;
import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.layout.AutoCompleteWindow;
import dev.cwby.graphics.layout.FloatingWindow;
import dev.cwby.graphics.layout.TiledWindow;
import dev.cwby.graphics.layout.Window;
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

    public static TiledWindow rootNode = new TiledWindow(0, 0, Engine.getWidth(), Engine.getHeight() - FontManager.getLineHeight(), null);
    public static Window currentWindow = rootNode;
    public static AutoCompleteWindow autoCompleteWindow = new AutoCompleteWindow(0, 0, 400, 0);

    public SkiaRenderer() {
        context = DirectContext.makeGL();

        textPaint = new Paint().setColor(Deditor.getConfig().treesitter.get("default"));
        rootNode.component = new TextComponent();
        onResize(Engine.getWidth(), Engine.getHeight());
        if (BufferManager.shouldOpenEmptyBuffer) {
            System.out.println("Opening empty buffer");
            currentWindow.component = new TextComponent().setBuffer(BufferManager.addEmptyBuffer());
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
        if (currentWindow instanceof FloatingWindow floatingWindow && currentWindow.isVisible()) {
            floatingWindow.getComponent().render(canvas, 0, 0, rootNode.width, rootNode.height);
        }
        renderAutoCompleteWindow(canvas);
        context.flush();
        surface.flushAndSubmit();
    }


    public void renderRegion(Canvas canvas, TiledWindow node) {
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
        if (autoCompleteWindow.isVisible() && currentWindow.component instanceof TextComponent textComponent) {
            var buffer = textComponent.getBuffer();
            autoCompleteWindow.render(canvas, buffer.cursorX, buffer.cursorY, rootNode.width, rootNode.height);
        }
    }

    public static void openFloatingWindow(FloatingWindow floatingWindow) {
        if (!floatingWindow.isVisible()) {
            floatingWindow.show(0, 0);
        }
        currentWindow = floatingWindow;
    }

    public static TextBuffer getCurrentTextBuffer() {
        return ((TextComponent) currentWindow.component).getBuffer();
    }
}
