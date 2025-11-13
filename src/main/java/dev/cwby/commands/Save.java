package dev.cwby.commands;

import dev.cwby.editor.TextBuffer;
import dev.cwby.graphics.OpenGLRenderer;
import dev.cwby.graphics.layout.component.TextComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Save implements ICommand {
    @Override
    public boolean run(String[] args) {
        var buffer = ((TextComponent) OpenGLRenderer.WM.getCurrentWindow().component).getBuffer();

        if (buffer instanceof TextBuffer textBuffer) {
            File file = textBuffer.file;
            if (file != null) {
                try {
                    Files.write(file.toPath(), buffer.getLines());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return true;
    }
}
