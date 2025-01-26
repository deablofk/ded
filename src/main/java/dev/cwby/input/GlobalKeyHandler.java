package dev.cwby.input;

import dev.cwby.Deditor;
import dev.cwby.editor.TextInteractionMode;

import static org.lwjgl.glfw.GLFW.*;

public class GlobalKeyHandler implements IKeyHandler {
    public static TextInteractionMode MODE = TextInteractionMode.NAVIGATION;

    @Override
    public void handleKey(int key, int action, int mods) {
        if (action == GLFW_RELEASE) {
            handleMode(key, action, mods);
        }
    }

    @Override
    public void handleChar(long codePoint) {
        if (MODE == TextInteractionMode.NAVIGATION) {
            if (codePoint == ':') {
                MODE = TextInteractionMode.COMMAND;
            }
        } else if (MODE == TextInteractionMode.INSERT) {
            Deditor.buffer.appendChar((char) codePoint);
        }
    }

    public void handleMode(int key, int action, int mods) {
        switch (MODE) {
            case NAVIGATION -> handleNavigation(key, action, mods);
            case SELECT -> handleSelect(key, action, mods);
            case INSERT -> handleInsert(key, action, mods);
            case COMMAND -> handleCommand(key, action, mods);
        }
    }

    public void handleInsert(int key, int action, int mods) {
        switch (key) {
            case GLFW_KEY_ESCAPE:
                MODE = TextInteractionMode.NAVIGATION;
                break;
            case GLFW_KEY_ENTER:
                Deditor.buffer.newLine();
                break;
        }
    }

    public void handleSelect(int key, int action, int mods) {
        if (action == GLFW_RELEASE) {
            switch (key) {
                case GLFW_KEY_ESCAPE:
                    MODE = TextInteractionMode.NAVIGATION;
                    break;
                case GLFW_KEY_Y:
                    // yank selected text
                    break;
                default:
                    break;
            }
        }
    }

    public void handleCommand(int key, int action, int mods) {
        switch (key) {
            case GLFW_KEY_ESCAPE:
                MODE = TextInteractionMode.NAVIGATION;
                break;
        }
    }

    public void handleNavigation(int key, int action, int mods) {
        if (key == GLFW_KEY_SEMICOLON && action == GLFW_PRESS && (mods & GLFW_MOD_SHIFT) != 0) {
            System.out.println(MODE.getName());
            MODE = TextInteractionMode.COMMAND;
            return;
        }

        switch (key) {
            case GLFW_KEY_ESCAPE, GLFW_KEY_Q:
                break;
            case GLFW_KEY_I:
                MODE = TextInteractionMode.INSERT;
                break;
            case GLFW_KEY_V:
                MODE = TextInteractionMode.SELECT;
                break;
            default:
                break;
        }
    }

}
