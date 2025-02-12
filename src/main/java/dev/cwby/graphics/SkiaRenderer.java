package dev.cwby.graphics;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.WindowManager;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
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
    public static final WindowManager WM = new WindowManager();

    public SkiaRenderer() {
        context = DirectContext.makeGL();

        textPaint = new Paint().setColor(Deditor.getConfig().treesitter.get("default"));
        onResize(Engine.getWidth(), Engine.getHeight());
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
        WM.getRootNode().updateSize(0, 0, width, height - FontManager.getLineHeight());
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
        renderTiledWindows(canvas, WM.getRootNode());
        renderStatusLine(0, height - FontManager.getLineHeight(), width, height);
        renderFloatingWindows();
        renderAutoCompleteWindow(canvas);
        context.flush();
        surface.flushAndSubmit();
    }


    public void renderTiledWindows(Canvas canvas, TiledWindow node) {
        if (node.isLeaf()) {
            if (node.component != null) {
                node.component.render(canvas, node.x, node.y, node.width, node.height);
            }
        } else {
            renderTiledWindows(canvas, node.leftChild);
            renderTiledWindows(canvas, node.rightChild);
        }
    }

    public void renderAutoCompleteWindow(Canvas canvas) {
        if (WM.getAutoCompleteWindow().isVisible() && WM.getCurrentWindow().component instanceof TextComponent textComponent) {
            System.out.println("Auto complete window is visible and componetn is text component");
            var buffer = textComponent.getBuffer();
            WM.getAutoCompleteWindow().render(canvas, buffer.cursorX, buffer.cursorY, WM.getRootNode().width, WM.getRootNode().height);
        }
    }

    public void renderFloatingWindows() {
        var windows = WM.getFloatingWindows();
        if (windows.isEmpty()) {
            return;
        }

        for (Window window : windows) {
            if (window.isVisible()) {
                window.component.render(canvas, 0, 0, WM.getRootNode().width, WM.getRootNode().height);
            }
        }
    }

    public static TextBuffer getCurrentTextBuffer() {
        return ((TextComponent) WM.getCurrentWindow().component).getBuffer();
    }
}
