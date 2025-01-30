package dev.cwby.input;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.TextComponent;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDL_Event;

import static dev.cwby.editor.TextInteractionMode.*;
import static org.lwjgl.sdl.SDLKeycode.*;

public class GlobalKeyHandler implements IKeyHandler {

    private int lastKey = -1;

    @Override
    public void handle(SDL_Event e) {
        TextInteractionMode mode = Deditor.getBufferMode();
        int keyCode = e.key().key();
        short mod = e.key().mod();
        int keyChar = SDLKeyboard.SDL_GetKeyFromScancode(e.key().scancode(), mod, false);

        if (mode == NAVIGATION) {
            handleNavigation((char) keyChar, keyCode, mod);
        } else if (mode == INSERT) {
            handleInsert((char) keyChar, keyCode, mod);
        } else if (mode == SELECT) {
            handleSelect((char) keyChar, keyCode, mod);
        } else if (mode == COMMAND) {
            handleCommand((char) keyChar, keyCode, mod);
        }
    }


    public void handleInsert(char keyChar, int keyCode, short mod) {
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
        switch (keyCode) {
            case SDLK_ESCAPE -> Deditor.setBufferMode(NAVIGATION);
            case SDLK_RETURN -> buffer.newLine();
            case SDLK_BACKSPACE -> buffer.removeChar();
            case SDLK_TAB -> {
                for (int i = 0; i < 4; i++) {
                    buffer.appendChar(' ');
                }
            }
            case SDLK_V -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    String clipboard = SDLClipboard.SDL_GetClipboardText();
                    buffer.insertTextAtCursor(clipboard);
                } else {
                    buffer.appendChar(keyChar);
                }
            }
            case SDLK_LSHIFT, SDLK_RSHIFT, SDLK_LCTRL, SDLK_RCTRL, SDLK_LALT, SDLK_RALT -> {
            }
            default -> buffer.appendChar(keyChar);
        }
    }

    public void handleSelect(char keyChar, int keyCode, short mod) {
        switch (keyChar) {
            case SDLK_ESCAPE -> Deditor.setBufferMode(NAVIGATION);
        }
    }

    public void handleCommand(char keyChar, int keyCode, short mod) {
        switch (keyCode) {
            case SDLK_ESCAPE -> {
                Deditor.setBufferMode(NAVIGATION);
                CommandHandler.clearCommandBuffer();
            }
            case SDLK_RETURN -> {
                CommandHandler.executeCommand(CommandHandler.getBuffer());
                CommandHandler.clearCommandBuffer();
                Deditor.setBufferMode(NAVIGATION);
            }
            case SDLK_BACKSPACE -> {
                int length = CommandHandler.getBuffer().length() - 1;
                if (length >= 0) {
                    CommandHandler.getBuilderBuffer().deleteCharAt(length);
                }
            }
            case SDLK_LSHIFT, SDLK_RSHIFT, SDLK_LCTRL, SDLK_RCTRL, SDLK_LALT, SDLK_RALT -> {
            }
            default -> CommandHandler.appendBuffer(keyChar);
        }
    }

    public void handleNavigation(int keyChar, int keyCode, short mod) {
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
        switch (keyChar) {
            case 'i' -> Deditor.setBufferMode(INSERT);
            case 'v' -> Deditor.setBufferMode(SELECT);
            case 'o' -> {
                buffer.newLineDown();
                Deditor.setBufferMode(INSERT);
            }
            case 'O' -> {
                buffer.newLineUp();
                Deditor.setBufferMode(INSERT);
            }
            case 'd' -> {
                if (lastKey == 'd') {
                    buffer.deleteCurrentLine();
                    lastKey = -1;
                } else {
                    lastKey = keyChar;
                }
            }
            case ':' -> Deditor.setBufferMode(COMMAND);
            case 'h' -> buffer.moveCursor(--buffer.cursorX, buffer.cursorY);
            case 'j' -> buffer.moveCursor(buffer.cursorX, ++buffer.cursorY);
            case 'k' -> buffer.moveCursor(buffer.cursorX, --buffer.cursorY);
            case 'l' -> buffer.moveCursor(++buffer.cursorX, buffer.cursorY);
        }
    }
}
