package dev.cwby.graphics.layout;

import dev.cwby.graphics.layout.component.IComponent;

public class Window {

    public float x, y, width, height;
    public String title;
    public IComponent component;

    public Window(String title, float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void open() {
    }

    public void close() {
    }

    public IComponent getComponent() {
        return component;
    }
}
