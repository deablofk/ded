package dev.cwby.input;

import dev.cwby.editor.ScratchBuffer;
import dev.cwby.graphics.layout.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TrieNode {
    public Map<String, TrieNode> children = new HashMap<>();
    public BiConsumer<Window, ScratchBuffer> action;
    public long lastPressTime;

    public TrieNode() {
        this.action = null;
        this.lastPressTime = System.currentTimeMillis();
    }

    public TrieNode search(String keybinding) {
        return children.get(keybinding);
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
