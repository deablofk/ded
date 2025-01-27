package dev.cwby.input;

import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;

import static dev.cwby.editor.TextInteractionMode.*;
import static org.lwjgl.glfw.GLFW.*;

public class GlobalKeyHandler implements IKeyHandler {

    private int lastKey = -1;

    @Override
    public void handleKey(int key, int action, int mods) {
        switch (Deditor.getMode()) {
            case NAVIGATION -> handleNavigation(key, action, mods);
            case SELECT -> handleSelect(key, action, mods);
            case INSERT -> handleInsert(key, action, mods);
            case COMMAND -> handleCommand(key, action, mods);
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
        if (action == GLFW_RELEASE || action == GLFW_REPEAT) {
            switch (key) {
                case GLFW_KEY_ESCAPE -> Deditor.setMode(NAVIGATION);
                case GLFW_KEY_ENTER -> Deditor.buffer.newLine();
                case GLFW_KEY_BACKSPACE -> Deditor.buffer.removeChar();
                case GLFW_KEY_TAB -> {
                    for (int i = 0; i < 4; i ++){
                        Deditor.buffer.appendChar(' ');
                    }
                }
            }
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
            case GLFW_KEY_BACKSPACE -> {
                if (action == GLFW_RELEASE || action == GLFW_REPEAT) {
                    int length = Deditor.commandBuffer.length() - 1;
                    if (length >= 0) {
                        Deditor.commandBuffer.deleteCharAt(length);
                    }
                }
            }
        }
    }

    public void handleNavigation(int key, int action, int mods) {
        TextBuffer buffer = Deditor.buffer;
        if (action == GLFW_RELEASE) {
            switch (key) {
                case GLFW_KEY_I -> Deditor.setMode(INSERT);
                case GLFW_KEY_V -> Deditor.setMode(SELECT);
                case GLFW_KEY_O -> {
                    if (mods == GLFW_MOD_SHIFT) {
                        Deditor.buffer.newLineUp();
                        Deditor.setMode(INSERT);
                    } else {
                        Deditor.buffer.newLineDown();
                        Deditor.setMode(INSERT);
                    }
                }
                case GLFW_KEY_D -> {
                    if (lastKey == GLFW_KEY_D) {
                        Deditor.buffer.deleteCurrentLine();
                        lastKey = -1;
                    } else {
                        lastKey = key;
                    }
                }
                case 47 -> {
                    if (mods == GLFW_MOD_SHIFT) Deditor.setMode(COMMAND);
                }
                default -> {
                }
            }
        } else {
            switch (key) {
                case GLFW_KEY_H -> buffer.moveCursor(--buffer.cursorX, buffer.cursorY);
                case GLFW_KEY_J -> buffer.moveCursor(buffer.cursorX, ++buffer.cursorY);
                case GLFW_KEY_K -> buffer.moveCursor(buffer.cursorX, --buffer.cursorY);
                case GLFW_KEY_L -> buffer.moveCursor(++buffer.cursorX, buffer.cursorY);
                default -> {
                }
            }
        }
    }
}
