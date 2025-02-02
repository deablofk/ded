package dev.cwby.commands;

import dev.cwby.graphics.Engine;
import dev.cwby.graphics.SkiaRenderer;

public class Quit implements ICommand {

    @Override
    public boolean run(String[] args) {
        if (SkiaRenderer.currentNode.father == null) {
            Engine.setShouldClose(true);
        } else {
            SkiaRenderer.currentNode.close();
        }
        return true;
    }
}
