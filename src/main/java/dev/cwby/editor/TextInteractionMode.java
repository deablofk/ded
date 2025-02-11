package dev.cwby.editor;

public enum TextInteractionMode {
    INSERT("INSERT-MODE"),
    SELECT("SELECT-MODE"),
    SELECT_LINE("SELECT-LINE-MODE"),
    SELECT_BLOCK("SELECT-BLOCK-MODE"),
    COMMAND("COMMAND-MODE"),
    NAVIGATION("NAVIGATION-MODE"),
    ANY("ANY-MODE");

    private final String name;

    TextInteractionMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
