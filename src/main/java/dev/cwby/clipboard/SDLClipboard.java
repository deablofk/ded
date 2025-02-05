package dev.cwby.clipboard;

public class SDLClipboard implements IClipboard {

    private String clipboard;

    @Override
    public void setClipboardText(String text) {
        if (!text.equals(clipboard)) {
            org.lwjgl.sdl.SDLClipboard.SDL_SetClipboardText(text);
        }
        this.clipboard = text;
    }

    @Override
    public String getClipboardText() {
        if (this.clipboard == null) {
            this.clipboard = org.lwjgl.sdl.SDLClipboard.SDL_GetClipboardText();
        }
        return clipboard;
    }
}