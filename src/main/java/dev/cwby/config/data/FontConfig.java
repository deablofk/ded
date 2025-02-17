package dev.cwby.config.data;

public class FontConfig {
    public String family;
    public int size;

    public FontConfig() {
    }

    public void increaseFontSize(int i) {
        this.size += i;
    }

}