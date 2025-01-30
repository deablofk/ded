package dev.cwby.input;

import org.lwjgl.sdl.SDL_Event;

public interface IKeyHandler {

    void handle(SDL_Event event);
}
