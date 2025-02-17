package dev.cwby.input;

import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.clipboard.ClipboardManager;
import dev.cwby.clipboard.ClipboardType;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.Engine;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.TiledWindow;
import dev.cwby.graphics.layout.Window;
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
            b.moveCursorRight();
            switchMode(INSERT);
        });
        KeybindingTrie.nmap("A", (_, b) -> {
            b.moveToLastNonWhitespaceChar();
            switchMode(INSERT);
        });
        KeybindingTrie.nmap("v", (_, b) -> {
            startVisualX = b.cursorX;
            startVisualY = b.cursorY;
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
        KeybindingTrie.nmap("g g", (w, b) -> b.moveCursor(0, 0));
        KeybindingTrie.nmap("G", (w, b) -> b.moveCursor(0, b.lines.size() - 1));
        KeybindingTrie.nmap("h", (w, b) -> b.moveCursorLeft());
        KeybindingTrie.nmap("j", (w, b) -> b.moveCursorDown());
        KeybindingTrie.nmap("k", (w, b) -> b.moveCursorUp());
        KeybindingTrie.nmap("l", (w, b) -> b.moveCursorRight());
        KeybindingTrie.nmap("w", (_, b) -> b.moveNextWord());
        KeybindingTrie.nmap("b", (_, b) -> b.movePreviousWord());
        KeybindingTrie.nmap("$", (_, b) -> b.gotoPosition(b.getCurrentLine().length() - 1, b.cursorY));
        KeybindingTrie.nmap("SHIFT-#", (_, b) -> b.searchWordUnderCursor());

        KeybindingTrie.nmap("CTRL-u", (w, b) -> b.moveCursorHalfUp());
        KeybindingTrie.nmap("CTRL-d", (w, b) -> b.moveCursorHalfDown());
        KeybindingTrie.nmap("p", (_, b) -> b.pasteText(ClipboardManager.getClipboardContent(ClipboardType.INTERNAL)));
        KeybindingTrie.nmap("CTRL-p", (w, b) -> b.moveCursorUp());
        KeybindingTrie.nmap("CTRL-n", (w, b) -> b.moveCursorDown());
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

        KeybindingTrie.nmap("CTRL-w v", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                CommandHandler.executeCommand("vs");
            }
        });

        KeybindingTrie.nmap("CTRL-w s", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                CommandHandler.executeCommand("s");
            }
        });

        KeybindingTrie.nmap("RET", (w, _) -> w.onTrigger());

        // lsp stuff, it is probably best to register only if there is a lsp in the buffer, but actually ded cant have specific buffers binding
        KeybindingTrie.nmap("g d", (w, b) -> {
            if (b instanceof TextBuffer textBuffer) {
                List<Location> definitions = LSPManager.getDefinitions(textBuffer.file.getAbsolutePath(), b.cursorY, b.cursorX);
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
        });

    }

    private static void registerSelectMappings() {
        KeybindingTrie.smap("ESC", (_, _) -> switchMode(NAVIGATION));
        KeybindingTrie.map(SELECT_LINE, "ESC", (_, _) -> switchMode(NAVIGATION));
        KeybindingTrie.smap("h", (w, b) -> b.moveCursorLeft());
        KeybindingTrie.smap("j", (w, b) -> b.moveCursorDown());
        KeybindingTrie.smap("k", (w, b) -> b.moveCursorUp());
        KeybindingTrie.smap("l", (w, b) -> b.moveCursorRight());
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
            if (SkiaRenderer.WM.getAutoCompleteWindow().isVisible()) {
                SkiaRenderer.WM.getAutoCompleteWindow().buffer.moveCursorUp();
            }
        });
        KeybindingTrie.imap("CTRL-n", (w, b) -> {
            if (SkiaRenderer.WM.getAutoCompleteWindow().isVisible()) {
                SkiaRenderer.WM.getAutoCompleteWindow().buffer.moveCursorDown();
            }
        });

        KeybindingTrie.imap("ESC", (_, _) -> {
            SkiaRenderer.WM.getAutoCompleteWindow().hide();
            switchMode(NAVIGATION);
        });
        KeybindingTrie.imap("RET", (_, b) -> {
            var cmpWindow = SkiaRenderer.WM.getAutoCompleteWindow();
            if (cmpWindow.isVisible()) {
                CompletionItem selectedItem = cmpWindow.select();
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
            SkiaRenderer.WM.getAutoCompleteWindow().hide();
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

        root = root.search(keyPressed);

        if (root == null) {
            root = KeybindingTrie.getRoot(Deditor.getBufferMode());
        } else if (root.action != null) {
            Window window = SkiaRenderer.WM.getCurrentWindow();
            ScratchBuffer buffer = null;
            if (window.getComponent() instanceof TextComponent textComponent) {
                buffer = textComponent.getBuffer();
                buffer.setVisibleLines(window.getVisibleLines());
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
        if (mode == INSERT && SkiaRenderer.WM.getCurrentWindow().getComponent() instanceof TextComponent textComponent) {
            ScratchBuffer buffer = textComponent.getBuffer();
            char c = event.text().textString().charAt(0);
            buffer.appendChar(c);

            if (c == '.' || Character.isLetterOrDigit(c)) {
                LSPManager.sendDidChangeNotification(buffer);
                List<CompletionItem> suggestions = LSPManager.onDotPressed(buffer.getFilepath(), buffer.cursorY, buffer.cursorX);
                if (suggestions.isEmpty()) {
                    return;
                }
                float windowX = SkiaRenderer.WM.getCurrentWindow().x + ((buffer.cursorX - buffer.offsetX) * FontManager.getAvgWidth());
                float windowY = SkiaRenderer.WM.getCurrentWindow().y + (buffer.cursorY - buffer.offsetY) * FontManager.getLineHeight();

                float maxWindowHeight = (Engine.getHeight() - FontManager.getLineHeight()) - windowY;
                float windowWidth = 400;

                var cmpWindow = SkiaRenderer.WM.getAutoCompleteWindow();
                cmpWindow.setSuggestions(suggestions);
                cmpWindow.show(windowX, windowY, windowWidth, maxWindowHeight);
            } else {
                SkiaRenderer.WM.getAutoCompleteWindow().hide();
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