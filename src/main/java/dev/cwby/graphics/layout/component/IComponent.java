package dev.cwby.graphics.layout.component;

import dev.cwby.graphics.opengl.Renderer2D;

public interface IComponent {
    void render(Renderer2D renderer, float x, float y, float width, float height);
}
