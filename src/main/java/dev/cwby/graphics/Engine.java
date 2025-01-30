package dev.cwby.graphics;

import dev.cwby.input.GlobalKeyHandler;
import dev.cwby.input.IKeyHandler;
import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDL_Event;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.sdl.SDLEvents.SDL_PollEvent;
import static org.lwjgl.sdl.SDLInit.*;
import static org.lwjgl.sdl.SDLVideo.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Engine {

    private static long window;
    private static int width = 1280;
    private static int height = 720;
    private final IKeyHandler keyHandler = new GlobalKeyHandler();
    private static boolean shouldClose;

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static long getWindow() {
        return window;
    }

    public static void setShouldClose(boolean shouldClose) {
        Engine.shouldClose = shouldClose;
    }

    public void initSDL() {
        if (!SDL_Init(SDL_INIT_VIDEO)) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        window = SDL_CreateWindow("Hello World!", width, height, SDL_WINDOW_OPENGL | SDL_WINDOW_BORDERLESS | SDL_WINDOW_RESIZABLE);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        SDL_GL_CreateContext(window);
        SDL_GL_MakeCurrent(window, SDL_GL_GetCurrentContext());
        SDL_GL_SetSwapInterval(1);
        GL.createCapabilities();

        IRender renderer = new SkiaRenderer();
        SDL_Event event = SDL_Event.create();
        while (!shouldClose) {
            // handle events
            while (SDL_PollEvent(event)) {
                switch (event.type()) {
                    case SDLEvents.SDL_EVENT_QUIT:
                        shouldClose = true;
                        break;
                    case SDLEvents.SDL_EVENT_KEY_DOWN:
                        keyHandler.handle(event);
                        break;
                    case SDLEvents.SDL_EVENT_TEXT_INPUT:
                        keyHandler.handleInput(event);
                        break;
                    case SDLEvents.SDL_EVENT_WINDOW_RESIZED:
                        var display = event.display();
                        width = display.data1();
                        height = display.data2();
                        renderer.onResize(width, height);
                        break;
                }
            }

            // render
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderer.render(width, height);

            SDL_GL_SwapWindow(window);
        }
        SDL_Quit();
    }
}
