package dev.cwby.input;

import dev.cwby.Deditor;

import static dev.cwby.editor.TextInteractionMode.*;
import static org.lwjgl.glfw.GLFW.*;

public class GlobalKeyHandler implements IKeyHandler {

    @Override
    public void handleKey(int key, int action, int mods) {
        if (action == GLFW_RELEASE) {
            switch (Deditor.getMode()) {
                case NAVIGATION -> handleNavigation(key, action, mods);
                case SELECT -> handleSelect(key, action, mods);
                case INSERT -> handleInsert(key, action, mods);
                case COMMAND -> handleCommand(key, action, mods);
            }
        }
    }

    @Override
    public void handleChar(long codePoint) {
        switch (Deditor.getMode()) {
            case COMMAND -> Deditor.commandBuffer.append((char) codePoint);
            case INSERT -> Deditor.buffer.appendChar((char) codePoint);
        }
    }

    public void handleInsert(int key, int action, int mods) {
        switch (key) {
            case GLFW_KEY_ESCAPE -> Deditor.setMode(NAVIGATION);
            case GLFW_KEY_ENTER -> Deditor.buffer.newLine();
        }
    }

    public void handleSelect(int key, int action, int mods) {
        switch (key) {
            case GLFW_KEY_ESCAPE -> Deditor.setMode(NAVIGATION);
            case GLFW_KEY_Y -> {
                // handle yank
                // initial
            }
            default -> {
            }
        }
    }


    public void handleCommand(int key, int action, int mods) {
        switch (key) {
            case GLFW_KEY_ESCAPE -> {
                Deditor.setMode(NAVIGATION);
                Deditor.clearCommandBuffer();
            }
            case GLFW_KEY_ENTER -> {
                Deditor.executeCommand(Deditor.commandBuffer.toString());
                Deditor.clearCommandBuffer();
                Deditor.setMode(NAVIGATION);
            }
        }
    }

    public void handleNavigation(int key, int action, int mods) {
        switch (key) {
            case GLFW_KEY_I -> Deditor.setMode(INSERT);
            case GLFW_KEY_V -> Deditor.setMode(SELECT);
            case 47 -> {
                if (mods == GLFW_MOD_SHIFT) Deditor.setMode(COMMAND);
            }
            default -> {
            }
        }
    }
}
