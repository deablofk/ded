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
    private static Map<String, Node> parsedCache = new HashMap<>();
    private static Map<String, Language> languageCache = new HashMap<>();

    public static Language loadTSLanguage(String libraryPath, String language) {
        if (languageCache.containsKey(language)) {
            return languageCache.get(language);
        }

        System.load(libraryPath);
        var loadedLanguage = Language.load(SymbolLookup.loaderLookup(), language);
        System.out.println("Loaded language " + language + " - " + loadedLanguage);
        languageCache.put(language, loadedLanguage);
        return loadedLanguage;
    }

    public static Node parse(String code, String fileType) {
        if (parsedCache.containsKey(code)) {
            return parsedCache.get(code);
        }

        String tsLanguage = TreeSitterLanguages.getTSFileFromFileType(fileType);
        String filePath = System.getProperty("user.dir") + "/config/highlight/" + tsLanguage + ".so";
        Parser parser = new Parser(loadTSLanguage(filePath, "tree_sitter_" + tsLanguage));
        Tree tree = parser.parse(code, InputEncoding.UTF_8).orElse(null);
        Node rootNode = tree.getRootNode();
        parsedCache.put(code, rootNode);
        parser.close();
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
