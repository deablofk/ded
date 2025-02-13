package dev.cwby;

import dev.cwby.graphics.Engine;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.layout.AutoCompleteWindow;
import dev.cwby.graphics.layout.FloatingWindow;
import dev.cwby.graphics.layout.TiledWindow;
import dev.cwby.graphics.layout.Window;
import dev.cwby.graphics.layout.component.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {
    private TiledWindow rootNode;
    private Window currentWindow;
    private TiledWindow currentTiledWindow;
    private final AutoCompleteWindow autoCompleteWindow;
    private final List<FloatingWindow> floatingWindows;

    public WindowManager() {
        this.rootNode = new TiledWindow(0, 0, Engine.getWidth(), Engine.getHeight() - FontManager.getLineHeight(), null);
        this.rootNode.component = new TextComponent().setBuffer(BufferManager.addEmptyBuffer());
        this.currentWindow = rootNode;
        this.autoCompleteWindow = new AutoCompleteWindow(0, 0, 400, 0);
        this.floatingWindows = new ArrayList<>();
    }

    public TiledWindow getRootNode() {
        return rootNode;
    }

    public void setRootNode(TiledWindow rootNode) {
        this.rootNode = rootNode;
    }

    public Window getCurrentWindow() {
        return currentWindow;
    }

    public AutoCompleteWindow getAutoCompleteWindow() {
        return autoCompleteWindow;
    }

    public List<FloatingWindow> getFloatingWindows() {
        return floatingWindows;
    }

    public void setCurrentWindow(Window currentWindow) {
        if (currentWindow instanceof TiledWindow tiledWindow) {
            this.currentTiledWindow = tiledWindow;
        }

        this.currentWindow = currentWindow;
    }

    public void openFloatingWindow(FloatingWindow window) {
        window.visible = true;
        if (currentWindow instanceof TiledWindow tiled) {
            currentTiledWindow = tiled;
        }
        floatingWindows.add(window);
        currentWindow = window;
    }

    public void closeFloatingWindow(FloatingWindow window) {
        window.visible = false;
        floatingWindows.remove(window);
        if (floatingWindows.isEmpty()) {
            currentWindow = currentTiledWindow;
        } else {
            currentWindow = window;
        }
    }
}