package dev.cwby.input;

public interface IKeyHandler {

    void handleKey(int key, int action, int mods);

    void handleChar(long codePoint);
}
