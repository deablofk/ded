package dev.cwby.graphics;

public interface IRender {
    void render(int width, int height);

    void onResize(int width, int height);
}
