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
import dev.cwby.graphics.layout.TiledWindow;
import dev.cwby.graphics.layout.Window;
import dev.cwby.graphics.layout.component.FZFComponent;
import dev.cwby.graphics.layout.component.TextComponent;
import dev.cwby.lsp.LSPManager;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDL_Event;

import java.util.List;

import static dev.cwby.editor.TextInteractionMode.*;
import static org.lwjgl.sdl.SDLKeycode.*;

public class GlobalKeyHandler implements IKeyHandler {

    private int lastKey = -1;
    private StringBuilder keySequence = new StringBuilder();
    private static final long SEQUENCE_TIMEOUT = 500;
    public static long lastKeyPressTime = 0;

    public static int startVisualX, startVisualY;

    public GlobalKeyHandler() {
        registerNormalMappings();
        registerSelectMappings();
        registerInsertMappings();
        registerCommandMappings();
    }


    private static void registerCommandMappings() {
        KeybindingTrie.cmap("ESC", (_, _) -> {
            CommandHandler.clearCommandBuffer();
            switchMode(NAVIGATION);
        });
        KeybindingTrie.cmap("RET", (_, _) -> {
            switchMode(NAVIGATION);
            CommandHandler.executeCommand(CommandHandler.getBuffer());
            CommandHandler.clearCommandBuffer();
        });
        KeybindingTrie.cmap("BACKSPACE", (_, _) -> {
            int length = CommandHandler.getBuffer().length() - 1;
            if (length >= 0) {
                CommandHandler.getBuilderBuffer().deleteCharAt(length);
            }
        });
        KeybindingTrie.cmap("CTRL-v", (w, b) -> {
            CommandHandler.getBuilderBuffer().append(ClipboardManager.getClipboardContent(ClipboardType.SYSTEM));
        });
    }

    private static void registerNormalMappings() {
        KeybindingTrie.nmap("i", (w, b) -> switchMode(INSERT));
        KeybindingTrie.nmap("I", (w, b) -> {
            b.moveToFirstNonWhitespaceChar();
            switchMode(INSERT);
        });
        KeybindingTrie.nmap("a", (w, b) -> {
            b.moveCursorRight(w.getVisibleLines());
            switchMode(INSERT);
        });
        KeybindingTrie.nmap("A", (_, b) -> {
            b.moveToLastNonWhitespaceChar();
            switchMode(INSERT);
        });
        KeybindingTrie.nmap("v", (_, b) -> {
            startVisualX = b.cursorX;
            startVisualY = b.cursorY;
            System.out.println(startVisualX + " " + startVisualY);
            switchMode(SELECT);
        });
        KeybindingTrie.nmap("V", (_, _) -> switchMode(SELECT_LINE));
        KeybindingTrie.nmap("SHIFT-:", (_, _) -> switchMode(COMMAND));
        KeybindingTrie.nmap("o", (_, b) -> {
            b.newLineDown();
            switchMode(INSERT);
        });
        KeybindingTrie.nmap("O", (_, b) -> {
            b.newLineUp();
            switchMode(INSERT);
        });

        KeybindingTrie.nmap("d d", (_, b) -> b.deleteCurrentLine());
        KeybindingTrie.nmap("h", (w, b) -> b.moveCursorLeft(w.getVisibleLines()));
        KeybindingTrie.nmap("j", (w, b) -> b.moveCursorDown(w.getVisibleLines()));
        KeybindingTrie.nmap("k", (w, b) -> b.moveCursorUp(w.getVisibleLines()));
        KeybindingTrie.nmap("l", (w, b) -> b.moveCursorRight(w.getVisibleLines()));
        KeybindingTrie.nmap("w", (_, b) -> b.moveNextWord());
        KeybindingTrie.nmap("b", (_, b) -> b.movePreviousWord());
        KeybindingTrie.nmap("$", (_, b) -> b.gotoPosition(b.getCurrentLine().length() - 1, b.cursorY));

        KeybindingTrie.nmap("CTRL-u", (w, b) -> b.moveCursorHalfUp(w.getVisibleLines()));
        KeybindingTrie.nmap("CTRL-d", (w, b) -> b.moveCursorHalfDown(w.getVisibleLines()));
        KeybindingTrie.nmap("p", (_, b) -> b.pasteText(ClipboardManager.getClipboardContent(ClipboardType.INTERNAL)));
        KeybindingTrie.nmap("CTRL-p", (w, _) -> {
            if (w.getComponent() != null) {
                ((FZFComponent) w.getComponent()).prev();
            }
        });
        KeybindingTrie.nmap("CTRL-n", (w, _) -> {
            if (w.getComponent() != null) {
                ((FZFComponent) w.getComponent()).next();
            }
        });
        KeybindingTrie.nmap("CTRL-w h", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                tiledWindow.moveLeft();
            }
        });

        KeybindingTrie.nmap("CTRL-w l", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                tiledWindow.moveRight();
            }
        });

        KeybindingTrie.nmap("CTRL-w j", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                tiledWindow.moveDown();
            }
        });

        KeybindingTrie.nmap("CTRL-w k", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                tiledWindow.moveUp();
            }
        });

        KeybindingTrie.nmap("RET", (w, b) -> {
            if (w != null && w.isVisible()) {
                String result = ((FZFComponent) w).select();
                CommandHandler.executeCommand("edit " + result);
            }
        });

        // lsp stuff, it is probably best to register only if there is a lsp in the buffer, but actualy ded cant have specific buffers binding
        KeybindingTrie.nmap("g d", (w, b) -> {
            List<Location> definitions = LSPManager.getDefinitions(b.file.getAbsolutePath(), b.cursorY, b.cursorX);
            if (definitions.size() == 1) {
                Location location = definitions.getFirst();
                CommandHandler.executeCommand("edit " + location.getUri().replace("file://", ""));
                int x = location.getRange().getStart().getCharacter();
                int y = location.getRange().getStart().getLine();
                SkiaRenderer.getCurrentTextBuffer().gotoPosition(x, y);
            } else {
                // show options in the floating window for selecting the denition
            }
        });

    }

    private static void registerSelectMappings() {
        KeybindingTrie.smap("ESC", (_, _) -> switchMode(NAVIGATION));
        KeybindingTrie.map(SELECT_LINE, "ESC", (_, _) -> switchMode(NAVIGATION));
        KeybindingTrie.smap("h", (w, b) -> b.moveCursorLeft(w.getVisibleLines()));
        KeybindingTrie.smap("j", (w, b) -> b.moveCursorDown(w.getVisibleLines()));
        KeybindingTrie.smap("k", (w, b) -> b.moveCursorUp(w.getVisibleLines()));
        KeybindingTrie.smap("l", (w, b) -> b.moveCursorRight(w.getVisibleLines()));
        KeybindingTrie.smap("y", (w, b) -> {
            String region = b.getRegion(startVisualX, startVisualY, b.cursorX, b.cursorY);
            ClipboardManager.setClipboardContent(ClipboardType.INTERNAL, region);
            switchMode(NAVIGATION);
        });
        KeybindingTrie.smap("d", (w, b) -> {
            String region = b.getRegion(startVisualX, startVisualY, b.cursorX, b.cursorY);
            ClipboardManager.setClipboardContent(ClipboardType.INTERNAL, region);
            b.deleteRegion(startVisualX, startVisualY, b.cursorX, b.cursorY);
            switchMode(NAVIGATION);
        });
    }

    private static void registerInsertMappings() {
        KeybindingTrie.imap("TAB", (w, b) -> b.insertTextAtCursor("    "));
        KeybindingTrie.imap("CTRL-p", (w, b) -> {
            if (SkiaRenderer.autoCompleteWindow.isVisible()) {
                SkiaRenderer.autoCompleteWindow.moveSelection(-1);
            } else if (w != null && w.isVisible()) {
                ((FZFComponent) w).prev();
            }
        });
        KeybindingTrie.imap("CTRL-n", (w, b) -> {
            if (SkiaRenderer.autoCompleteWindow.isVisible()) {
                SkiaRenderer.autoCompleteWindow.moveSelection(1);
            } else if (w != null && w.isVisible()) {
                ((FZFComponent) w).next();
            }
        });

        KeybindingTrie.imap("ESC", (_, _) -> {
            SkiaRenderer.autoCompleteWindow.hide();
            switchMode(NAVIGATION);
        });
        KeybindingTrie.imap("RET", (_, b) -> {
            if (SkiaRenderer.autoCompleteWindow.isVisible()) {
                CompletionItem selectedItem = SkiaRenderer.autoCompleteWindow.select();
                if (selectedItem != null) {
                    Either<TextEdit, InsertReplaceEdit> eitherTextEdit = selectedItem.getTextEdit();
                    if (eitherTextEdit.getLeft() != null) {
                        TextEdit edit = eitherTextEdit.getLeft();
                        b.replaceTextInRange(edit.getRange(), edit.getNewText());
                        if (selectedItem.getKind() == CompletionItemKind.Constructor || selectedItem.getKind() == CompletionItemKind.Method) {
                            b.insertTextAtCursor("()");
                        }
                    } else {
                        // TODO: insert text at cursor
                    }
                }
            } else {
                b.smartNewLine();
            }
        });
        KeybindingTrie.imap("BACKSPACE", (w, b) -> {
            b.removeChar();
            SkiaRenderer.autoCompleteWindow.hide();
        });

        KeybindingTrie.imap("CTRL-v", (w, b) -> {
            b.insertTextAtCursor(ClipboardManager.getClipboardContent(ClipboardType.SYSTEM));
        });
    }

    public static void switchMode(TextInteractionMode mode) {
        if (Deditor.getBufferMode() == mode) {
            return;
        }
        if (mode == INSERT || mode == COMMAND) {
            startTextInput();
        } else if (mode == SELECT || mode == SELECT_LINE || mode == SELECT_BLOCK) {
            stopTextInput();
        } else {
            stopTextInput();
        }

        Deditor.setBufferMode(mode);
    }

    TrieNode root = KeybindingTrie.getRoot(Deditor.getBufferMode());

    @Override
    public void handle(SDL_Event e) {
        int keyCode = e.key().key();
        short mod = e.key().mod();
        int keyChar = SDLKeyboard.SDL_GetKeyFromScancode(e.key().scancode(), mod, false);
        String keyPressed = getKey(mod, keyCode, (char) keyChar);
        System.out.println(keyPressed);

        root = root.search(keyPressed);

        System.out.println(root);

        if (root == null) {
            root = KeybindingTrie.getRoot(Deditor.getBufferMode());
        } else if (root.action != null) {
            Window window = SkiaRenderer.currentWindow;
            TextBuffer buffer = null;
            if (window.getComponent() instanceof TextComponent textComponent) {
                buffer = textComponent.getBuffer();
            }
            root.action.accept(window, buffer);
            root = KeybindingTrie.getRoot(Deditor.getBufferMode());
        }

        lastKeyPressTime = System.currentTimeMillis();
    }

    public String getKey(short mod, int keyCode, char keyChar) {
        if (keyCode == SDLK_ESCAPE) {
            return "ESC";
        } else if (keyCode == SDLK_RETURN) {
            return "RET";
        } else if (keyCode == SDLK_BACKSPACE) {
            return "BACKSPACE";
        } else if (keyCode == SDLK_TAB) {
            return "TAB";
        } else if ((mod & SDL_KMOD_CTRL) != 0) {
            return "CTRL-" + keyChar;
        } else if ((mod & SDL_KMOD_SHIFT) != 0) {
            if (Character.isUpperCase(keyChar)) {
                return String.valueOf(keyChar);
            } else {
                return "SHIFT-" + keyChar;
            }
        } else if ((mod & SDL_KMOD_ALT) != 0) {
            return "ALT-" + keyChar;
        } else {
            return String.valueOf(keyChar);
        }
    }

    @Override
    public void handleInput(SDL_Event event) {
        TextInteractionMode mode = Deditor.getBufferMode();
        if (mode == INSERT && SkiaRenderer.currentWindow.getComponent() instanceof TextComponent textComponent) {
            TextBuffer buffer = textComponent.getBuffer();
            char c = event.text().textString().charAt(0);
            buffer.appendChar(c);

            if (c == '.' || Character.isLetterOrDigit(c)) {
                float windowX = SkiaRenderer.currentWindow.x;
                float windowY = SkiaRenderer.currentWindow.y;

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

    public static void stopTextInput() {
        SDLKeyboard.SDL_StopTextInput(Engine.getWindow());
    }

    public static void startTextInput() {
        SDLKeyboard.SDL_StartTextInput(Engine.getWindow());
    }

}