package dev.cwby;

import dev.cwby.config.ConfigurationParser;
import dev.cwby.config.data.EditorConfig;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.Engine;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.input.GlobalKeyHandler;

import static dev.cwby.editor.TextInteractionMode.NAVIGATION;

public class Deditor {
    public static Engine engine = new Engine();
    private static TextInteractionMode MODE = NAVIGATION;
    public static EditorConfig config;

    public void start() {
        config = ConfigurationParser.readConfiguration("config/config.toml");
        if (config == null) {
            throw new RuntimeException("Could not load config.toml");
        }
        engine.setKeyHandler(new GlobalKeyHandler());
        engine.initGLFW();
        engine.setRenderer(new SkiaRenderer());
        engine.loop();
        engine.destroyGLFW();
    }

    public static void setBufferMode(TextInteractionMode mode) {
        MODE = mode;
    }

    public static TextInteractionMode getBufferMode() {
        return MODE;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            BufferManager.shouldOpenEmptyBuffer = true;
        }
        Deditor deditor = new Deditor();
        deditor.start();
    }
}
