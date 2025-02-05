package dev.cwby.clipboard;

public class ClipboardManager {

    private static IClipboard systemClipboard = new SDLClipboard();
    private static IClipboard internClipboard = new InternalClipboard();

    public static IClipboard getClipboard(ClipboardType type) {
        return type == ClipboardType.SYSTEM ? systemClipboard : internClipboard;
    }

    public static String getClipboardContent(ClipboardType type) {
        return getClipboard(type).getClipboardText();
    }

    public static void setClipboardContent(ClipboardType type, String text) {
        getClipboard(type).setClipboardText(text);
    }

}