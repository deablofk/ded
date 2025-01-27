package dev.cwby.commands;

import dev.cwby.Deditor;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class Quit implements ICommand {

    @Override
    public boolean run(String[] args) {
        glfwSetWindowShouldClose(Deditor.engine.window, true);
        return true;
    }
}
