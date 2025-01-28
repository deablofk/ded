package dev.cwby.input;

import dev.cwby.BufferManager;
import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;

import static dev.cwby.editor.TextInteractionMode.*;
import static org.lwjgl.glfw.GLFW.*;

public class GlobalKeyHandler implements IKeyHandler {

    private int lastKey = -1;

    @Override
    public void handleKey(int key, int action, int mods) {
        switch (Deditor.getBufferMode()) {
            case NAVIGATION -> handleNavigation(key, action, mods);
            case SELECT -> handleSelect(key, action, mods);
            case INSERT -> handleInsert(key, action, mods);
            case COMMAND -> handleCommand(key, action, mods);
        }
    }

    @Override
    public void handleChar(long codePoint) {
        switch (Deditor.getBufferMode()) {
            case COMMAND -> CommandHandler.appendBuffer((char) codePoint);
            case INSERT -> BufferManager.getActualBuffer().insertCharAtCursor((char) codePoint);
        }
    }

    public void handleInsert(int key, int action, int mods) {
        if (action == GLFW_RELEASE || action == GLFW_REPEAT) {
            switch (key) {
                case GLFW_KEY_ESCAPE -> Deditor.setBufferMode(NAVIGATION);
                case GLFW_KEY_ENTER -> BufferManager.getActualBuffer().newLine();
                case GLFW_KEY_BACKSPACE -> BufferManager.getActualBuffer().removeChar();
                case GLFW_KEY_TAB -> {
                    for (int i = 0; i < 4; i++) {
                        BufferManager.getActualBuffer().appendChar(' ');
                    }
                }
            }
        }
    }

    public void handleSelect(int key, int action, int mods) {
        switch (key) {
            case GLFW_KEY_ESCAPE -> Deditor.setBufferMode(NAVIGATION);
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
                Deditor.setBufferMode(NAVIGATION);
                CommandHandler.clearCommandBuffer();
            }
            case GLFW_KEY_ENTER -> {
                CommandHandler.executeCommand(CommandHandler.getBuffer());
                CommandHandler.clearCommandBuffer();
                Deditor.setBufferMode(NAVIGATION);
            }
            case GLFW_KEY_BACKSPACE -> {
                if (action == GLFW_RELEASE || action == GLFW_REPEAT) {
                    int length = CommandHandler.getBuffer().length() - 1;
                    if (length >= 0) {
                        CommandHandler.getBuilderBuffer().deleteCharAt(length);
                    }
                }
            }
        }
    }

    public void handleNavigation(int key, int action, int mods) {
        TextBuffer buffer = BufferManager.getActualBuffer();
        if (action == GLFW_RELEASE) {
            switch (key) {
                case GLFW_KEY_I -> Deditor.setBufferMode(INSERT);
                case GLFW_KEY_V -> Deditor.setBufferMode(SELECT);
                case GLFW_KEY_O -> {
                    if (mods == GLFW_MOD_SHIFT) {
                        BufferManager.getActualBuffer().newLineUp();
                        Deditor.setBufferMode(INSERT);
                    } else {
                        BufferManager.getActualBuffer().newLineDown();
                        Deditor.setBufferMode(INSERT);
                    }
                }
                case GLFW_KEY_D -> {
                    if (lastKey == GLFW_KEY_D) {
                        BufferManager.getActualBuffer().deleteCurrentLine();
                        lastKey = -1;
                    } else {
                        lastKey = key;
                    }
                }
                case 47 -> {
                    if (mods == GLFW_MOD_SHIFT) Deditor.setBufferMode(COMMAND);
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
