package dev.cwby.commands;

import dev.cwby.graphics.OpenGLRenderer;

public class Quit implements ICommand {

    @Override
    public boolean run(String[] args) {
        OpenGLRenderer.WM.getCurrentWindow().close();
        return true;
    }
}
