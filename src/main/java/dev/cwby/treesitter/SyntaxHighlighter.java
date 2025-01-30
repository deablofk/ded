package dev.cwby.treesitter;

import dev.cwby.Deditor;
import io.github.humbleui.skija.Paint;
import io.github.treesitter.jtreesitter.*;

import java.lang.foreign.SymbolLookup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntaxHighlighter {
    private static Language TSJava;
    private static Map<String, Node> parsedCache = new HashMap<>();

    static {
        System.load(System.getProperty("user.dir") + "/libtree-sitter-java.so");
        TSJava = Language.load(SymbolLookup.loaderLookup(), "tree_sitter_java");
    }

    public static Node parse(String code) {
        if (parsedCache.containsKey(code)) {
            return parsedCache.get(code);
        }

        Parser parser = new Parser(TSJava);
        Tree tree = parser.parse(code, InputEncoding.UTF_8).orElse(null);
        Node rootNode = tree.getRootNode();
        parsedCache.put(code, rootNode);
        return rootNode;
    }

    public static Map<Integer, Paint> highlight(Node root, String code) {
        Map<Integer, Paint> styles = new HashMap<>();
        List<HighlightSpan> highlights = traverseTree(root, code);
        for (HighlightSpan span : highlights) {
            for (int i = span.start(); i < span.end(); i++) {
                styles.put(i, span.paint());
            }
        }
        return styles;
    }

    public static List<HighlightSpan> traverseTree(Node node, String code) {
        List<HighlightSpan> spans = new ArrayList<>();
        String type = node.getType();
        int start = node.getStartByte();
        int end = node.getEndByte();
        Paint paint = getPaintForType(type);

        spans.add(new HighlightSpan(start, end, paint));

        for (Node child : node.getChildren()) {
            spans.addAll(traverseTree(child, code));
        }

        return spans;
    }

    private static Paint getPaintForType(String type) {
        Paint paint = new Paint();
        Map<String, Integer> mapTheme = Deditor.getConfig().treesitter;
        paint.setColor(mapTheme.getOrDefault(type, mapTheme.getOrDefault("default", 0xFFFFFFFF)));
        return paint;
    }
}
