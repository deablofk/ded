package dev.cwby.input;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.Engine;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.RegionNode;
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


    @Override
    public void handleInput(SDL_Event event) {
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
        TextInteractionMode mode = Deditor.getBufferMode();
        if (mode == INSERT) {
            buffer.appendChar(event.text().textString().charAt(0));
        } else if (mode == COMMAND) {
            CommandHandler.appendBuffer(event.text().textString().charAt(0));
        }
    }

    public void stopTextInput() {
        SDLKeyboard.SDL_StopTextInput(Engine.getWindow());
    }

    public void startTextInput() {
        SDLKeyboard.SDL_StartTextInput(Engine.getWindow());
    }

    public void handleInsert(char keyChar, int keyCode, short mod) {
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
        switch (keyCode) {
            case SDLK_ESCAPE -> {
                stopTextInput();
                Deditor.setBufferMode(NAVIGATION);
            }
            case SDLK_RETURN -> buffer.newLine();
            case SDLK_BACKSPACE -> buffer.removeChar();
            case SDLK_DELETE -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    System.out.println("removing word");
                    buffer.removeWordAfterCursor();
                } else {
                    buffer.removeCharAfterCursor();
                }
            }
            case SDLK_TAB -> {
                for (int i = 0; i < 4; i++) {
                    buffer.appendChar(' ');
                }
            }
            case SDLK_V -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    String clipboard = SDLClipboard.SDL_GetClipboardText();
                    buffer.insertTextAtCursor(clipboard);
                }
            }
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
                stopTextInput();
                CommandHandler.clearCommandBuffer();
                Deditor.setBufferMode(NAVIGATION);
            }
            case SDLK_RETURN -> {
                stopTextInput();
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
        }
    }

    public void handleNavigation(int keyChar, int keyCode, short mod) {
        RegionNode node = SkiaRenderer.currentNode;
        TextComponent component = (TextComponent) node.component;
        TextBuffer buffer = component.getBuffer();
        int visibleLines = (int) (node.height / FontManager.getLineHeight());
        switch (keyChar) {
            case 'i' -> {
                startTextInput();
                Deditor.setBufferMode(INSERT);
            }
            case 'v' -> Deditor.setBufferMode(SELECT);
            case 'o' -> {
                startTextInput();
                buffer.newLineDown();
                Deditor.setBufferMode(INSERT);
            }
            case 'O' -> {
                startTextInput();
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
            case ':' -> {
                startTextInput();
                Deditor.setBufferMode(COMMAND);
            }
            case 'h' -> buffer.moveCursor(--buffer.cursorX, buffer.cursorY, visibleLines);
            case 'j' -> buffer.moveCursor(buffer.cursorX, ++buffer.cursorY, visibleLines);
            case 'k' -> buffer.moveCursor(buffer.cursorX, --buffer.cursorY, visibleLines);
            case 'l' -> buffer.moveCursor(++buffer.cursorX, buffer.cursorY, visibleLines);
        }
    }
}
