package dev.cwby.input;

import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.layout.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class KeybindingTrie {

    private static Map<TextInteractionMode, TrieNode> roots = new HashMap<TextInteractionMode, TrieNode>();

    public static TrieNode getRoot(TextInteractionMode mode) {
        TrieNode node = roots.get(mode);

        if (node == null) {
            node = new TrieNode();
            roots.putIfAbsent(mode, node);
        }

        return node;
    }

    private static void insertKeybinding(TextInteractionMode mode, String keybinding, BiConsumer<Window, TextBuffer> action) {
        TrieNode currentNode = getRoot(mode);
        String[] keys = keybinding.split(" ");

        for (String key : keys) {
            if (currentNode.children.containsKey(key)) {
                currentNode = currentNode.children.get(key);
            } else {
                var newNode = new TrieNode();
                currentNode.children.put(key, newNode);
                currentNode = newNode;
            }
        }

        currentNode.action = action;
    }

    public static void nmap(String keybinding, BiConsumer<Window, TextBuffer> action) {
        insertKeybinding(TextInteractionMode.NAVIGATION, keybinding, action);
    }

    public static void imap(String keybinding, BiConsumer<Window, TextBuffer> action) {
        insertKeybinding(TextInteractionMode.INSERT, keybinding, action);
    }

    public static void smap(String keybinding, BiConsumer<Window, TextBuffer> action) {
        insertKeybinding(TextInteractionMode.SELECT, keybinding, action);
    }

    public static void cmap(String keybinding, BiConsumer<Window, TextBuffer> action) {
        insertKeybinding(TextInteractionMode.COMMAND, keybinding, action);
    }

    public static void map(TextInteractionMode mode, String keybinding, BiConsumer<Window, TextBuffer> action) {
        insertKeybinding(mode, keybinding, action);
    }

}