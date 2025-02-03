package dev.cwby.input;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.Engine;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.WindowNode;
import dev.cwby.graphics.layout.component.TextComponent;
import dev.cwby.lsp.LSPManager;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDL_Event;

import java.util.List;

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
            char c = event.text().textString().charAt(0);
            buffer.appendChar(c);

            LSPManager.sendDidChangeNotification(buffer);
            List<CompletionItem> suggestions = LSPManager.onDotPressed(buffer.fileChunkLoader.getFile().getAbsolutePath(), buffer.cursorY, buffer.cursorX);
            SkiaRenderer.floatingWindow.setSuggestions(suggestions);
            SkiaRenderer.floatingWindow.show(buffer.cursorX * FontManager.getAvgWidth(), buffer.cursorY * FontManager.getLineHeight());
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
                SkiaRenderer.floatingWindow.hide();
            }
            case SDLK_RETURN -> {
                if (SkiaRenderer.floatingWindow.isVisible()) {
                    CompletionItem selectedItem = SkiaRenderer.floatingWindow.select();
                    System.out.println(selectedItem.toString());

                    Either<TextEdit, InsertReplaceEdit> eitherTextEdit = selectedItem.getTextEdit();
                    if (eitherTextEdit.getLeft() != null) {
                        TextEdit edit = eitherTextEdit.getLeft();
                        buffer.replaceTextInRange(edit.getRange(), edit.getNewText());
                        if (selectedItem.getKind() == CompletionItemKind.Constructor || selectedItem.getKind() == CompletionItemKind.Method) {
                            buffer.insertTextAtCursor("()");
                            buffer.cursorX += 2;
                        }
                    } else {
                        // TODO: insert text at cursor
                    }
                } else {
                    buffer.smartNewLine();
                }
            }
            case SDLK_BACKSPACE -> {
                buffer.removeChar();
                SkiaRenderer.floatingWindow.hide();
            }
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

            case SDLK_V -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    String clipboard = SDLClipboard.SDL_GetClipboardText();
                    CommandHandler.getBuilderBuffer().append(clipboard);
                }
            }
        }
    }

    public void handleNavigation(int keyChar, int keyCode, short mod) {
        WindowNode node = SkiaRenderer.currentNode;
        TextComponent component = (TextComponent) node.component;
        TextBuffer buffer = component.getBuffer();
        int visibleLines = (int) (node.height / FontManager.getLineHeight());

        // movement
        if ((mod & SDL_KMOD_ALT) != 0) {
            WindowNode newNode = switch (keyCode) {
                case SDLK_H -> node.moveLeft();
                case SDLK_L -> node.moveRight();
                case SDLK_J -> node.moveDown();
                case SDLK_K -> node.moveUp();
                default -> null;
            };

            if (newNode != null) {
                return;
            }
        }
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
                } else if (lastKey == 'g') {
                    List<Location> definitions = LSPManager.getDefinitions(buffer.file.getAbsolutePath(), buffer.cursorY, buffer.cursorX);
                    if (definitions.size() == 1) {
                        Location location = definitions.getFirst();
                        CommandHandler.executeCommand("edit " + location.getUri().replace("file://", ""));
                    } else {
                        // show options in the floating window for selecting the denition
                    }
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

        lastKey = keyChar;
    }
}
