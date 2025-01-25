package dev.cwby;

import dev.cwby.graphics.Engine;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.input.GlobalKeyHandler;

public class Deditor {
    public static Engine engine = new Engine();

    public void start() {
        engine.setKeyHandler(new GlobalKeyHandler());
        engine.initGLFW();
        engine.setRenderer(new SkiaRenderer());
        engine.loop();
        engine.destroyGLFW();
    }
}
