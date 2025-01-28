package dev.cwby.commands;


import dev.cwby.BufferManager;
import dev.cwby.editor.FileChunkLoader;
import dev.cwby.editor.TextBuffer;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.TextComponent;

import java.io.File;

public class Edit implements ICommand {

    @Override
    public boolean run(String[] args) {
        if (args.length >= 2) {
            System.out.println(args[1]);
            File file = new File(args[1]);
            if (file.exists()) {
                if (SkiaRenderer.rootNode.component instanceof TextComponent textComponent) {
                    TextBuffer textBuffer = new TextBuffer(new FileChunkLoader(file, 10240));
                    BufferManager.addBuffer(textBuffer);
                    textComponent.setBuffer(textBuffer);
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
