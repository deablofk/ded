package dev.cwby.graphics.layout;

import dev.cwby.graphics.layout.component.IComponent;

public abstract class FloatingWindow implements IComponent {
    public float x;
    public float y;
    public float width;
    public float height;
    public boolean visible = false;

    public FloatingWindow(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void show(float x, float y) {
        this.x = x;
        this.y = y;
        this.visible = true;
    }

    public void show(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }
}