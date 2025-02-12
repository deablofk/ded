package dev.cwby.graphics.layout;


import dev.cwby.graphics.SkiaRenderer;

public abstract class FloatingWindow extends Window {

    public FloatingWindow(float x, float y, float width, float height) {
        super("", x, y, width, height);
    }

    public void show(float x, float y) {
        this.x = x;
        this.y = y;
        this.visible = true;
    }

    @Override
    public void close() {
        SkiaRenderer.WM.closeFloatingWindow(this);
        onClose();
    }
}