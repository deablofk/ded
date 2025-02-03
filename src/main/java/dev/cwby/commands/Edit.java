package dev.cwby.commands;

import dev.cwby.BufferManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.TextComponent;
import dev.cwby.lsp.LSPManager;

public class Edit implements ICommand {

    @Override
    public boolean run(String[] args) {
        if (args.length < 2) {
            System.out.println("Specify the File Path");
            return false;
        }
        SkiaRenderer.currentNode.component = new TextComponent().setBuffer(BufferManager.openFileBuffer(args[1]));
        return true;
    }

}
