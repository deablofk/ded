package dev.cwby.treesitter;

import java.util.HashMap;
import java.util.Map;

public class TreeSitterLanguages {
    private static Map<String, String> languages = new HashMap<>();

    static {
        languages.put("c", "c");
        languages.put("cs", "c_sharp");
        languages.put("cpp", "cpp");
        languages.put("css", "css");
        languages.put("dockerfile", "dockerfile");
        languages.put("gitignore", "gitignore");
        languages.put("go", "go");
        languages.put("html", "html");
        languages.put("java", "java");
        languages.put("js", "javascript");
        languages.put("json", "json");
        languages.put("kts", "kotlin");
        languages.put("lua", "lua");
        languages.put("md", "markdown");
        languages.put("php", "php");
        languages.put("py", "python");
        languages.put("rs", "rust");
        languages.put("toml", "toml");
        languages.put("ts", "typescript");
        languages.put("xml", "xml");
        languages.put("yaml", "yaml");
    }

    public static String getTSFileFromFileType(String fileType) {
        return languages.getOrDefault(fileType, "java");
    }

}
