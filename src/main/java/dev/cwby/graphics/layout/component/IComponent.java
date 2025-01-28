package dev.cwby.graphics.layout.component;

import io.github.humbleui.skija.Canvas;

public interface IComponent {
    void render(Canvas canvas, float x, float y, float width, float height);
}
