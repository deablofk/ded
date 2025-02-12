package dev.cwby.commands;

import dev.cwby.graphics.SkiaRenderer;

public class Quit implements ICommand {

    @Override
    public boolean run(String[] args) {
        SkiaRenderer.WM.getCurrentWindow().close();
        return true;
    }
}
