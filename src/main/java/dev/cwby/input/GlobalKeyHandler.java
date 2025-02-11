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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static dev.cwby.editor.TextInteractionMode.*;
import static org.lwjgl.sdl.SDLKeycode.*;

public class GlobalKeyHandler implements IKeyHandler {

    private int lastKey = -1;
    private StringBuilder keySequence = new StringBuilder();
    private static final long SEQUENCE_TIMEOUT = 500;
    public static long lastKeyPressTime = 0;

    private static final Map<String, BiConsumer<Window, TextBuffer>> navigationMappings = new HashMap<>();
    private static final Map<String, BiConsumer<Window, TextBuffer>> insertMappings = new HashMap<>();
    private static final Map<String, BiConsumer<Window, TextBuffer>> selectMappings = new HashMap<>();
    private static final Map<String, BiConsumer<Window, TextBuffer>> commandMappings = new HashMap<>();
    private static final Map<String, BiConsumer<Window, TextBuffer>> globalMappings = new HashMap<>();
    public static int startVisualX, startVisualY;

    public GlobalKeyHandler() {
        registerNormalMappings();
        registerSelectMappings();
        registerInsertMappings();
        registerCommandMappings();
    }

    public static void map(TextInteractionMode mode, String key, BiConsumer<Window, TextBuffer> consumer) {
        switch (mode) {
            case NAVIGATION -> navigationMappings.put(key, consumer);
            case INSERT -> insertMappings.put(key, consumer);
            case SELECT, SELECT_LINE, SELECT_BLOCK -> selectMappings.put(key, consumer);
            case COMMAND -> commandMappings.put(key, consumer);
            case ANY -> globalMappings.put(key, consumer);
        }
    }

    public static void nmap(String key, BiConsumer<Window, TextBuffer> consumer) {
        map(NAVIGATION, key, consumer);
    }

    public static void imap(String key, BiConsumer<Window, TextBuffer> consumer) {
        map(INSERT, key, consumer);
    }

    public static void smap(String key, BiConsumer<Window, TextBuffer> consumer) {
        map(SELECT, key, consumer);
    }

    public static void cmap(String key, BiConsumer<Window, TextBuffer> consumer) {
        map(COMMAND, key, consumer);
    }

    public static void map(String key, BiConsumer<Window, TextBuffer> consumer) {
        map(ANY, key, consumer);
    }

    private static void registerCommandMappings() {
        cmap("ESC", (_, _) -> {
            CommandHandler.clearCommandBuffer();
            switchMode(NAVIGATION);
        });
        cmap("RET", (_, _) -> {
            switchMode(NAVIGATION);
            CommandHandler.executeCommand(CommandHandler.getBuffer());
            CommandHandler.clearCommandBuffer();
        });
        cmap("BACKSPACE", (_, _) -> {
            int length = CommandHandler.getBuffer().length() - 1;
            if (length >= 0) {
                CommandHandler.getBuilderBuffer().deleteCharAt(length);
            }
        });
        cmap("Ctrl-v", (w, b) -> {
            CommandHandler.getBuilderBuffer().append(ClipboardManager.getClipboardContent(ClipboardType.SYSTEM));
        });
    }

    private static void registerNormalMappings() {
        nmap("i", (_, _) -> switchMode(INSERT));
        nmap("I", (_, b) -> {
            b.moveToFirstNonWhitespaceChar();
            switchMode(INSERT);
        });
        nmap("a", (w, b) -> {
            b.moveCursorRight(w.getVisibleLines());
            switchMode(INSERT);
        });
        nmap("A", (_, b) -> {
            b.moveToLastNonWhitespaceChar();
            switchMode(INSERT);
        });
        nmap("v", (_, b) -> {
            startVisualX = b.cursorX;
            startVisualY = b.cursorY;
            System.out.println(startVisualX + " " + startVisualY);
            switchMode(SELECT);
        });
        nmap("V", (_, _) -> switchMode(SELECT_LINE));
        nmap("Shift-:", (_, _) -> switchMode(COMMAND));
        nmap("o", (_, b) -> {
            b.newLineDown();
            switchMode(INSERT);
        });
        nmap("O", (_, b) -> {
            b.newLineUp();
            switchMode(INSERT);
        });
        nmap("dd", (_, b) -> b.deleteCurrentLine());
        nmap("h", (w, b) -> b.moveCursorLeft(w.getVisibleLines()));
        nmap("j", (w, b) -> b.moveCursorDown(w.getVisibleLines()));
        nmap("k", (w, b) -> b.moveCursorUp(w.getVisibleLines()));
        nmap("l", (w, b) -> b.moveCursorRight(w.getVisibleLines()));
        nmap("w", (_, b) -> b.moveNextWord());
        nmap("b", (_, b) -> b.movePreviousWord());
        nmap("$", (_, b) -> b.gotoPosition(b.getCurrentLine().length() - 1, b.cursorY));

        nmap("Ctrl-u", (w, b) -> b.moveCursorHalfUp(w.getVisibleLines()));
        nmap("Ctrl-d", (w, b) -> b.moveCursorHalfDown(w.getVisibleLines()));
        nmap("p", (_, b) -> b.pasteText(ClipboardManager.getClipboardContent(ClipboardType.INTERNAL)));
        nmap("Ctrl-p", (w, _) -> {
            if (w.getComponent() != null) {
                ((FZFComponent) w.getComponent()).prev();
            }
        });
        nmap("Ctrl-n", (w, _) -> {
            if (w.getComponent() != null) {
                ((FZFComponent) w.getComponent()).next();
            }
        });
        nmap("Alt-H", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                tiledWindow.moveLeft();
            }
        });

        nmap("Alt-L", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                tiledWindow.moveRight();
            }
        });

        nmap("Alt-J", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                tiledWindow.moveDown();
            }
        });

        nmap("Alt-K", (w, b) -> {
            if (w instanceof TiledWindow tiledWindow) {
                tiledWindow.moveUp();
            }
        });

        nmap("RET", (w, b) -> {
            if (w != null && w.isVisible()) {
                String result = ((FZFComponent) w).select();
                CommandHandler.executeCommand("edit " + result);
            }
        });

        // lsp stuff, it is probably best to register only if there is a lsp in the buffer, but actualy ded cant have specific buffers binding
        nmap("gd", (w, b) -> {
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
        smap("ESC", (_, _) -> switchMode(NAVIGATION));
        smap("h", (w, b) -> b.moveCursorLeft(w.getVisibleLines()));
        smap("j", (w, b) -> b.moveCursorDown(w.getVisibleLines()));
        smap("k", (w, b) -> b.moveCursorUp(w.getVisibleLines()));
        smap("l", (w, b) -> b.moveCursorRight(w.getVisibleLines()));
        smap("y", (w, b) -> {
            String region = b.getRegion(startVisualX, startVisualY, b.cursorX, b.cursorY);
            ClipboardManager.setClipboardContent(ClipboardType.INTERNAL, region);
            switchMode(NAVIGATION);
        });
        smap("d", (w, b) -> {
            String region = b.getRegion(startVisualX, startVisualY, b.cursorX, b.cursorY);
            ClipboardManager.setClipboardContent(ClipboardType.INTERNAL, region);
            b.deleteRegion(startVisualX, startVisualY, b.cursorX, b.cursorY);
            switchMode(NAVIGATION);
        });
    }

    private static void registerInsertMappings() {
        imap("TAB", (w, b) -> b.insertTextAtCursor("    "));
        imap("Ctrl-p", (w, b) -> {
            if (SkiaRenderer.autoCompleteWindow.isVisible()) {
                SkiaRenderer.autoCompleteWindow.moveSelection(-1);
            } else if (w != null && w.isVisible()) {
                ((FZFComponent) w).prev();
            }
        });
        imap("Ctrl-n", (w, b) -> {
            if (SkiaRenderer.autoCompleteWindow.isVisible()) {
                SkiaRenderer.autoCompleteWindow.moveSelection(1);
            } else if (w != null && w.isVisible()) {
                ((FZFComponent) w).next();
            }
        });

        imap("ESC", (_, _) -> {
            SkiaRenderer.autoCompleteWindow.hide();
            switchMode(NAVIGATION);
        });
        imap("RET", (_, b) -> {
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
        imap("BACKSPACE", (w, b) -> {
            b.removeChar();
            SkiaRenderer.autoCompleteWindow.hide();
        });

        imap("Ctrl-v", (w, b) -> {
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

    @Override
    public void handle(SDL_Event e) {
        int keyCode = e.key().key();
        short mod = e.key().mod();
        int keyChar = SDLKeyboard.SDL_GetKeyFromScancode(e.key().scancode(), mod, false);
        String keyPressed = getKeyCombination(mod, keyCode, (char) keyChar);
        System.out.println("keyPressed: " + keyPressed);

        handleModeSpecificMapping(keyPressed);

        lastKeyPressTime = System.currentTimeMillis();
    }

    public void handleModeSpecificMapping(String pressed) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastKeyPressTime > SEQUENCE_TIMEOUT) {
            keySequence.setLength(0);
        }

        keySequence.append(pressed);
        lastKeyPressTime = currentTime;

        TextInteractionMode mode = Deditor.getBufferMode();
        if (mode == NAVIGATION) {
            handleKey(pressed, navigationMappings);
        } else if (mode == INSERT) {
            handleKey(pressed, insertMappings);
        } else if (mode == SELECT || mode == SELECT_LINE || mode == SELECT_BLOCK) {
            handleKey(pressed, selectMappings);
        } else if (mode == COMMAND) {
            handleKey(pressed, commandMappings);
        }

        handleKey(pressed, globalMappings);
    }

    public void handleKey(String keyPressed, Map<String, BiConsumer<Window, TextBuffer>> map) {
        String key = keyPressed;
        if (map.containsKey(keySequence.toString())) {
            System.out.println(keySequence.toString());
            key = keySequence.toString();
        }
        var biConsumer = map.getOrDefault(key, null);

        if (biConsumer != null) {
            Window window = SkiaRenderer.currentWindow;
            TextBuffer buffer = null;
            if (window.getComponent() instanceof TextComponent textComponent) {
                buffer = textComponent.getBuffer();
            }
            biConsumer.accept(window, buffer);
        }

    }

    public String getKeyCombination(short mod, int keyCode, char keyChar) {
        if (keyCode == SDLK_ESCAPE) {
            return "ESC";
        } else if (keyCode == SDLK_RETURN) {
            return "RET";
        } else if (keyCode == SDLK_BACKSPACE) {
            return "BACKSPACE";
        } else if (keyCode == SDLK_TAB) {
            return "TAB";
        } else if ((mod & SDL_KMOD_CTRL) != 0) {
            return "Ctrl-" + keyChar;
        } else if ((mod & SDL_KMOD_SHIFT) != 0) {
            if (Character.isUpperCase(keyChar)) {
                return String.valueOf(keyChar);
            } else {
                return "Shift-" + keyChar;
            }
        } else if ((mod & SDL_KMOD_ALT) != 0) {
            return "Alt-" + keyChar;
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