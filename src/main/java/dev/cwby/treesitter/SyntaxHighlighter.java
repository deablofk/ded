package dev.cwby.treesitter;

import io.github.humbleui.skija.Paint;
import io.github.treesitter.jtreesitter.*;

import java.lang.foreign.SymbolLookup;
import java.util.HashMap;
import java.util.Map;

public class SyntaxHighlighter {
    private static Language TSJava;

    static {
        System.load("/home/cwby/jtreesitter/libparser.so");
        TSJava = Language.load(SymbolLookup.loaderLookup(), "tree_sitter_java");
    }

    public static Node parse(String code) {
        Parser parser = new Parser(TSJava);
        Tree tree = parser.parse(code, InputEncoding.UTF_8).orElse(null);
        return tree.getRootNode();
    }

    public static Map<Integer, Paint> highlight(Node root, String code) {
        Map<Integer, Paint> styles = new HashMap<>();
        traverseTree(root, styles, code);
        return styles;
    }

    public static void traverseTree(Node node, Map<Integer, Paint> styles, String code) {
        String type = node.getType();
        int start = node.getStartByte();
        int end = node.getEndByte();

        Paint paint = getPaintForType(type);
        for (int i = start; i < end; i++) {
            styles.put(i, paint);
        }

        for (Node child : node.getChildren()) {
            traverseTree(child, styles, code);
        }
    }

    private static Paint getPaintForType(String type) {
        Paint paint = new Paint();
        switch (type) {
            case "program":
                paint.setColor(0xFF000099); // Blue
                break;
            case "local_variable_declaration":
                paint.setColor(0xFF000077); // Blue
                break;
            case "type_identifier":
                paint.setColor(0xFF000055); // Blue
                break;
            case "variable_declarator":
                paint.setColor(0xFF009900); // Blue
                break;
            case "identifier":
                paint.setColor(0xFF007700); // Blue
                break;
            case "=":
                paint.setColor(0xFF005500); // Blue
                break;
            case "string_literal":
                paint.setColor(0xFF990000); // Blue
                break;
            case "\"":
                paint.setColor(0xFF770000); // Blue
                break;
            case "string_fragment":
                paint.setColor(0xFF550000); // Blue
                break;
            case ";":
                paint.setColor(0xFF009999); // Blue
                break;
            default:
                paint.setColor(0xFFA86432); // Default white
        }
        return paint;
    }

}
