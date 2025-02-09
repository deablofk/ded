package dev.cwby.commands;

import dev.cwby.graphics.SkiaRenderer;

public class Quit implements ICommand {

    @Override
    public boolean run(String[] args) {
        SkiaRenderer.currentWindow.close();
        return true;
    }
}
