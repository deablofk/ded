package dev.cwby;

import dev.cwby.config.ConfigurationParser;
import dev.cwby.config.data.EditorConfig;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.exceptions.NoConfigFoundException;
import dev.cwby.graphics.Engine;

public class Deditor {
    private static Engine engine;
    private static EditorConfig config;
    private static TextInteractionMode MODE;

    public static void setBufferMode(TextInteractionMode mode) {
        MODE = mode;
    }

    public static TextInteractionMode getBufferMode() {
        return MODE;
    }

    public static EditorConfig getConfig() {
        return config;
    }

    public static Engine getEngine() {
        return engine;
    }

    public static void main(String[] args) {
        config = ConfigurationParser.read("config/config.toml").orElseThrow(NoConfigFoundException::new);
        setBufferMode(TextInteractionMode.NAVIGATION);
        engine = new Engine();
        engine.initSDL();
    }
}
