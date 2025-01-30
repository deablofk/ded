package dev.cwby.commands;

import dev.cwby.graphics.Engine;

public class Quit implements ICommand {

    @Override
    public boolean run(String[] args) {
        Engine.shouldClose = true;
        return true;
    }
}
