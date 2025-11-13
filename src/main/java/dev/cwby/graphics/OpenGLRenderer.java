package dev.cwby.graphics;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.WindowManager;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.layout.TiledWindow;
import dev.cwby.graphics.layout.Window;
import dev.cwby.graphics.layout.component.TextComponent;
import dev.cwby.graphics.opengl.Renderer2D;

public class OpenGLRenderer implements IRender {
    private Renderer2D renderer;
    public static final WindowManager WM = new WindowManager();
    private int currentWidth;
    private int currentHeight;

    public OpenGLRenderer() {
        currentWidth = Engine.getWidth();
        currentHeight = Engine.getHeight();
        renderer = new Renderer2D(currentWidth, currentHeight);
    }

    @Override
    public void onResize(int width, int height) {
        currentWidth = width;
        currentHeight = height;
        renderer.updateProjection(width, height);
        WM.getRootNode().updateSize(0, 0, width, height - FontManager.getLineHeight());
    }

    public void renderStatusLine(float x, float y, float width, float height) {
        renderer.pushClip(x, y, width, height);
        renderer.drawRect(x, y, width, height, 0xFF000000);
        
        String statusText;
        if (Deditor.getBufferMode() == TextInteractionMode.COMMAND) {
            statusText = ":" + CommandHandler.getBuffer();
        } else {
            statusText = Deditor.getBufferMode().toString();
        }
        
        int textColor = Deditor.getConfig().treesitter.get("default");
        renderer.drawText(statusText, 5, y + FontManager.getLineHeight() - 5, FontManager.getDefaultFont(), textColor);
        renderer.popClip();
    }

    @Override
    public void render(int width, int height) {
        renderer.clear(0xFF000000);
        renderer.startFrame();
        
        renderTiledWindows(WM.getRootNode());
        renderStatusLine(0, height - FontManager.getLineHeight(), width, FontManager.getLineHeight());
        renderFloatingWindows();
        renderAutoCompleteWindow();
        
        renderer.endFrame();
    }

    public void renderTiledWindows(TiledWindow node) {
        if (node.isLeaf()) {
            if (node.component != null) {
                node.component.render(renderer, node.x, node.y, node.width, node.height);
            }
        } else {
            renderTiledWindows(node.leftChild);
            renderTiledWindows(node.rightChild);
        }
    }

    public void renderAutoCompleteWindow() {
        var cmpWindow = WM.getAutoCompleteWindow();
        if (!cmpWindow.isVisible()) {
            return;
        }
        cmpWindow.getComponent().render(renderer, cmpWindow.x, cmpWindow.y, cmpWindow.width, cmpWindow.height);
    }

    public void renderFloatingWindows() {
        var windows = WM.getFloatingWindows();
        if (windows.isEmpty()) {
            return;
        }

        for (Window window : windows) {
            if (window.isVisible()) {
                window.component.render(renderer, window.x, window.y, window.width, window.height);
            }
        }
    }

    public static ScratchBuffer getCurrentTextBuffer() {
        return ((TextComponent) WM.getCurrentWindow().component).getBuffer();
    }

    public void cleanup() {
        renderer.cleanup();
    }
}