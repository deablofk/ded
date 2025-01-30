package dev.cwby.commands;

import dev.cwby.editor.TextBuffer;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.TextComponent;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class Save implements ICommand {
    @Override
    public boolean run(String[] args) {
//        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
//
//        File file = buffer.file;
//        if (file != null) {
//            List<String> lines = buffer.getLines();
//            try {
//                Files.write(file.toPath(), lines);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            return true;
//        }
//
        return true;
    }
}
