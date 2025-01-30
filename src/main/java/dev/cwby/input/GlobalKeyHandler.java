package dev.cwby.input;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.Engine;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.TextComponent;

import static dev.cwby.editor.TextInteractionMode.*;
import static org.lwjgl.glfw.GLFW.*;

public class GlobalKeyHandler implements IKeyHandler {

    private int lastKey = -1;

    @Override
    public void handleKey(int key, int scanCode, int action, int mods) {
        TextInteractionMode mode = Deditor.getBufferMode();

        if (mode == NAVIGATION) {
            handleNavigation(key, action, mods);
        } else if (mode == INSERT) {
            handleInsert(key, scanCode, action, mods);
        } else if (mode == SELECT) {
            handleSelect(key, action, mods);
        } else if (mode == COMMAND) {
            handleCommand(key, action, mods);
        }
    }

    @Override
    public void handleChar(long codePoint) {
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
        switch (Deditor.getBufferMode()) {
            case COMMAND -> CommandHandler.appendBuffer((char) codePoint);
            case INSERT -> {
                if (codePoint != 'V' && codePoint != 'v') {
                    buffer.insertCharAtCursor((char) codePoint);
                }
            }
        }
    }

    public void handleInsert(int key, int scancode, int action, int mods) {
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
        if (action == GLFW_RELEASE || action == GLFW_REPEAT) {
            switch (key) {
                case GLFW_KEY_ESCAPE -> Deditor.setBufferMode(NAVIGATION);
                case GLFW_KEY_ENTER -> buffer.newLine();
                case GLFW_KEY_BACKSPACE -> buffer.removeChar();
                case GLFW_KEY_TAB -> {
                    for (int i = 0; i < 4; i++) {
                        buffer.appendChar(' ');
                    }
                }
                case GLFW_KEY_V -> {
                    if (mods == GLFW_MOD_CONTROL) {
                        String clipboard = glfwGetClipboardString(Engine.window);
                        buffer.insertTextAtCursor(clipboard);
                    } else {
                        String keyName = glfwGetKeyName(key, scancode);
                        if (keyName != null) {
                            if (mods == GLFW_MOD_SHIFT) {
                                buffer.appendChar(keyName.toLowerCase().charAt(0));
                            } else {
                                buffer.appendChar(keyName.charAt(0));
                            }
                        }
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
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
        if (action == GLFW_RELEASE) {
            switch (key) {
                case GLFW_KEY_I -> Deditor.setBufferMode(INSERT);
                case GLFW_KEY_V -> Deditor.setBufferMode(SELECT);
                case GLFW_KEY_O -> {
                    if (mods == GLFW_MOD_SHIFT) {
                        buffer.newLineUp();
                        Deditor.setBufferMode(INSERT);
                    } else {
                        buffer.newLineDown();
                        Deditor.setBufferMode(INSERT);
                    }
                }
                case GLFW_KEY_D -> {
                    if (lastKey == GLFW_KEY_D) {
                        buffer.deleteCurrentLine();
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
