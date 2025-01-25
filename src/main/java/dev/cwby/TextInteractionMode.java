package dev.cwby;

public enum TextInteractionMode {
    INSERT("INSERT-MODE"),
    SELECT("SELECT-MODE"),
    COMMAND("COMMAND-MODE"),
    NAVIGATION("NAVIGATION-MODE");

    private final String name;

    TextInteractionMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
