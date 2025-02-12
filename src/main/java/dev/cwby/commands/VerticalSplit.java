package dev.cwby.commands;

import dev.cwby.BufferManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.TiledWindow;
import dev.cwby.graphics.layout.Window;
import dev.cwby.graphics.layout.component.TextComponent;

public class VerticalSplit implements ICommand {

    @Override
    public boolean run(String[] args) {
        Window window = SkiaRenderer.WM.getCurrentWindow();
        if (window instanceof TiledWindow tiledWindow) {
            tiledWindow.splitVertically();
            if (tiledWindow.component != null) {
                tiledWindow.leftChild.component = tiledWindow.component;
                tiledWindow.rightChild.component = tiledWindow.component;
            } else {
                TextComponent component = new TextComponent().setBuffer(BufferManager.addEmptyBuffer());
                tiledWindow.leftChild.component = component;
                tiledWindow.rightChild.component = component;
            }
            SkiaRenderer.WM.setCurrentWindow(tiledWindow.rightChild);
        }
        return true;
    }
}
