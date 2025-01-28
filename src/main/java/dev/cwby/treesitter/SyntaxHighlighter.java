package dev.cwby.treesitter;

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

        switch (type) {
            // Program Structure
            case "program":
                paint.setColor(0xFF000099); // Dark Blue (Program Structure)
                break;

            // Variable Declaration and Identifiers
            case "local_variable_declaration":
                paint.setColor(0xFFCC7832); // Orange (Variables)
                break;
            case "type_identifier":
                paint.setColor(0xFF4E90F0); // Light Blue (Types)
                break;
            case "variable_declarator":
            case "identifier":
                paint.setColor(0xFF9876AA); // Purple (Identifiers)
                break;

            // Keywords
            case "public":
            case "private":
            case "protected":
            case "static":
            case "final":
            case "class":
            case "interface":
            case "enum":
            case "if":
            case "else":
            case "while":
            case "for":
            case "return":
            case "break":
            case "continue":
            case "void":
            case "int":
            case "long":
            case "double":
            case "float":
            case "boolean":
            case "new":
            case "import":
            case "package":
                paint.setColor(0xFFFFC66D); // Yellow (Keywords)
                break;

            // Operators
            case "=":
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case "==":
            case "!=":
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "&&":
            case "||":
            case "!":
                paint.setColor(0xFFCC7832);
                break;

            // Strings and Characters
            case "string_literal":
            case "\"":
            case "string_fragment":
                paint.setColor(0xFF6A8759); // Green (Strings)
                break;
            case "character_literal":
                paint.setColor(0xFF6A8759); // Green (Characters)
                break;

            // Numbers
            case "decimal_integer_literal":
            case "floating_point_literal":
                paint.setColor(0xFF6897BB); // Light Blue (Numbers)
                break;

            // Punctuation
            case ";":
            case ",":
            case ".":
                paint.setColor(0xFFCC7832); // Orange (Punctuation)
                break;

            // Comments
            case "comment":
            case "line_comment":
            case "block_comment":
                paint.setColor(0xFF808080); // Gray (Comments)
                break;

            // Annotations
            case "annotation":
                paint.setColor(0xFFBBB529); // Yellowish-Green (Annotations)
                break;

            // Default
            default:
                paint.setColor(0xFFA9B7C6); // Default IntelliJ Light Gray
        }

        return paint;
    }
}
