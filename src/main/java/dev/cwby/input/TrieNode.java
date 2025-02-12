package dev.cwby.input;

import dev.cwby.editor.TextBuffer;
import dev.cwby.graphics.layout.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TrieNode {
    public Map<String, TrieNode> children = new HashMap<>();
    public BiConsumer<Window, TextBuffer> action;
    public long lastPressTime;

    public TrieNode() {
        this.action = null;
        this.lastPressTime = System.currentTimeMillis();

    }

    public TrieNode search(String keybinding) {
        var node = children.get(keybinding);
        return node;
    }

    @Override
    public String toString() {
        return "TrieNode{" +
                "children=" + children +
                ", action=" + action +
                ", lastPressTime=" + lastPressTime +
                '}';
    }
}
