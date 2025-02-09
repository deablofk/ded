package dev.cwby.commands;

import dev.cwby.editor.TextBuffer;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.TextComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Save implements ICommand {
    @Override
    public boolean run(String[] args) {
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentWindow.component).getBuffer();

        File file = buffer.file;
        if (file != null) {
            try {
                Files.write(file.toPath(), buffer.getLines());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return true;
    }
}
