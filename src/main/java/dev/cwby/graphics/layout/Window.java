package dev.cwby.graphics.layout;

import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.layout.component.IComponent;

public class Window {

    public float x, y, width, height;
    public String title;
    public IComponent component;
    public boolean visible = false;

    public Window(String title, float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
    }

    // behavior (composition)
    public void open() {
    }

    public void close() {
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public IComponent getComponent() {
        return component;
    }

    public void setComponent(IComponent component) {
        this.component = component;
    }

    public int getVisibleLines() {
        return (int) (height / FontManager.getLineHeight());
    }

}
