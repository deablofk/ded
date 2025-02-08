package dev.cwby.input;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.clipboard.ClipboardManager;
import dev.cwby.clipboard.ClipboardType;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.Engine;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.WindowNode;
import dev.cwby.graphics.layout.component.FZFComponent;
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
    public static long lastKeyPressTime = 0;  // Track the last key press time

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
        lastKeyPressTime = System.currentTimeMillis();
    }


    @Override
    public void handleInput(SDL_Event event) {
        TextBuffer buffer = ((TextComponent) SkiaRenderer.currentNode.component).getBuffer();
        TextInteractionMode mode = Deditor.getBufferMode();
        if (mode == INSERT) {
            char c = event.text().textString().charAt(0);
            buffer.appendChar(c);

            if (c == '.' || Character.isLetterOrDigit(c)) {
                float windowX = SkiaRenderer.currentNode.x;
                float windowY = SkiaRenderer.currentNode.y;

                LSPManager.sendDidChangeNotification(buffer);
                List<CompletionItem> suggestions = LSPManager.onDotPressed(buffer.fileChunkLoader.getFile().getAbsolutePath(), buffer.cursorY, buffer.cursorX);
                SkiaRenderer.autoCompleteWindow.setSuggestions(suggestions);
                SkiaRenderer.autoCompleteWindow.show(windowX + ((buffer.cursorX - buffer.offsetX) * FontManager.getAvgWidth()), windowY + (buffer.cursorY - buffer.offsetY) * FontManager.getLineHeight());
            } else {
                SkiaRenderer.autoCompleteWindow.hide();
            }
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
                SkiaRenderer.autoCompleteWindow.hide();
            }
            case SDLK_RETURN -> {
                if (SkiaRenderer.autoCompleteWindow.isVisible()) {
                    CompletionItem selectedItem = SkiaRenderer.autoCompleteWindow.select();
                    if (selectedItem != null) {
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
                    }
                } else {
                    buffer.smartNewLine();
                }
            }
            case SDLK_BACKSPACE -> {
                buffer.removeChar();
                SkiaRenderer.autoCompleteWindow.hide();
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
                    buffer.insertTextAtCursor(ClipboardManager.getClipboardContent(ClipboardType.SYSTEM));
                }
            }
            case SDLK_P -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    if (SkiaRenderer.autoCompleteWindow.isVisible()) {
                        SkiaRenderer.autoCompleteWindow.moveSelection(-1);
                    } else if (SkiaRenderer.floatingWindow != null && SkiaRenderer.floatingWindow.isVisible()) {
                        ((FZFComponent) SkiaRenderer.floatingWindow).prev();
                    }
                }
            }
            case SDLK_N -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    if (SkiaRenderer.autoCompleteWindow.isVisible()) {
                        SkiaRenderer.autoCompleteWindow.moveSelection(1);
                    } else if (SkiaRenderer.floatingWindow != null && SkiaRenderer.floatingWindow.isVisible()) {
                        ((FZFComponent) SkiaRenderer.floatingWindow).next();
                    }
                }
            }
        }
    }

    public static int startVisualX, startVisualY;

    public void handleSelect(char keyChar, int keyCode, short mod) {
        WindowNode node = SkiaRenderer.currentNode;
        TextBuffer buffer = SkiaRenderer.getCurrentTextBuffer();
        int visibleLines = (int) (node.height / FontManager.getLineHeight());
        if (keyCode == SDLK_ESCAPE) {
            Deditor.setBufferMode(NAVIGATION);
            return;
        }
        switch (keyChar) {
            case 'h' -> buffer.moveCursor(--buffer.cursorX, buffer.cursorY, visibleLines);
            case 'j' -> buffer.moveCursor(buffer.cursorX, ++buffer.cursorY, visibleLines);
            case 'k' -> buffer.moveCursor(buffer.cursorX, --buffer.cursorY, visibleLines);
            case 'l' -> buffer.moveCursor(++buffer.cursorX, buffer.cursorY, visibleLines);
            case 'y' -> {
                String region = buffer.getRegion(startVisualX, startVisualY, buffer.cursorX, buffer.cursorY);
                ClipboardManager.setClipboardContent(ClipboardType.INTERNAL, region);
                Deditor.setBufferMode(NAVIGATION);
            }
            case 'd' -> {
                String region = buffer.getRegion(startVisualX, startVisualY, buffer.cursorX, buffer.cursorY);
                ClipboardManager.setClipboardContent(ClipboardType.INTERNAL, region);
                buffer.deleteRegion(startVisualX, startVisualY, buffer.cursorX, buffer.cursorY);
                Deditor.setBufferMode(NAVIGATION);
            }
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

        switch (keyChar) {
            case 'u' -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    buffer.moveCursor(buffer.cursorX, buffer.cursorY - visibleLines / 2, visibleLines);
                }
            }
            case 'i' -> {
                startTextInput();
                Deditor.setBufferMode(INSERT);
            }
            case 'v' -> {
                startVisualX = buffer.cursorX;
                startVisualY = buffer.cursorY;
                Deditor.setBufferMode(SELECT);
            }
            case 'V' -> {
                startVisualX = 0;
                startVisualY = buffer.cursorY;
                Deditor.setBufferMode(SELECT);
            }
            case 'p' -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    if (SkiaRenderer.floatingWindow != null && SkiaRenderer.floatingWindow.isVisible()) {
                        ((FZFComponent) SkiaRenderer.floatingWindow).prev();
                    }
                } else {
                    buffer.pasteText(ClipboardManager.getClipboardContent(ClipboardType.INTERNAL));
                }
            }
            case 'n' -> {
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    if (SkiaRenderer.floatingWindow != null && SkiaRenderer.floatingWindow.isVisible()) {
                        ((FZFComponent) SkiaRenderer.floatingWindow).next();
                    }
                }
            }
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
                if ((mod & SDL_KMOD_CTRL) != 0) {
                    buffer.moveCursor(buffer.cursorX, buffer.cursorY + visibleLines / 2, visibleLines);
                } else if (lastKey == 'd') {
                    buffer.deleteCurrentLine();
                } else if (lastKey == 'g') {
                    List<Location> definitions = LSPManager.getDefinitions(buffer.file.getAbsolutePath(), buffer.cursorY, buffer.cursorX);
                    if (definitions.size() == 1) {
                        Location location = definitions.getFirst();
                        CommandHandler.executeCommand("edit " + location.getUri().replace("file://", ""));
                        int x = location.getRange().getStart().getCharacter();
                        int y = location.getRange().getStart().getLine();
                        SkiaRenderer.getCurrentTextBuffer().gotoPosition(x, y);
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
            case 'w' -> buffer.moveNextWord();
            case 'b' -> buffer.movePreviousWord();
            case '$' -> buffer.gotoPosition(buffer.getCurrentLine().length() - 1, buffer.cursorY);
            case 'A' -> {
                buffer.gotoPosition(buffer.getCurrentLine().length(), buffer.cursorY);
                startTextInput();
                Deditor.setBufferMode(INSERT);
            }
            case 'I' -> {
                char c = buffer.getCurrentLine().toString().trim().charAt(0);
                int index = buffer.getCurrentLine().indexOf(c + "");
                buffer.gotoPosition(index, buffer.cursorY);
                startTextInput();
                Deditor.setBufferMode(INSERT);
            }
            // Window Movement (ALT + H/J/K/L)
            case 'H', 'J', 'K', 'L' -> {
                if ((mod & SDL_KMOD_ALT) != 0) {
                    switch (keyChar) {
                        case 'H' -> node.moveLeft();
                        case 'L' -> node.moveRight();
                        case 'J' -> node.moveDown();
                        case 'K' -> node.moveUp();
                    }
                }
            }
        }

        switch (keyCode) {
            case SDLK_RETURN -> {
                if (SkiaRenderer.floatingWindow != null && SkiaRenderer.floatingWindow.isVisible()) {
                    String result = ((FZFComponent)SkiaRenderer.floatingWindow).select();
                    CommandHandler.executeCommand("edit " + result);
                }
            }
        }


        lastKey = keyChar;
    }
}
