package dev.cwby.clipboard;

public class InternalClipboard implements IClipboard {

    private String clipboard;

    @Override
    public void setClipboardText(String text) {
        this.clipboard = text;
    }

    @Override
    public String getClipboardText() {
        return this.clipboard;
    }

}