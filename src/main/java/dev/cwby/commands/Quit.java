package dev.cwby.commands;

import dev.cwby.graphics.Engine;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.lsp.LSPManager;

public class Quit implements ICommand {

    @Override
    public boolean run(String[] args) {
        if (SkiaRenderer.currentNode.father == null) {
            LSPManager.closeAllLsp();
            Engine.setShouldClose(true);
        } else {
            SkiaRenderer.currentNode.close();
        }
        return true;
    }
}
