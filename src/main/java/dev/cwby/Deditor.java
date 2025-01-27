package dev.cwby;

import dev.cwby.commands.Edit;
import dev.cwby.commands.ICommand;
import dev.cwby.commands.Quit;
import dev.cwby.commands.Save;
import dev.cwby.editor.TextBuffer;
import dev.cwby.editor.TextInteractionMode;
import dev.cwby.graphics.Engine;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.input.GlobalKeyHandler;

import java.util.HashMap;
import java.util.Map;

import static dev.cwby.editor.TextInteractionMode.NAVIGATION;

public class Deditor {
    public static Engine engine = new Engine();
    public static TextBuffer buffer = new TextBuffer();
    public static StringBuilder commandBuffer = new StringBuilder();
    private static TextInteractionMode MODE = NAVIGATION;

    private static Map<String, ICommand> COMMANDS = new HashMap<>();

    public Deditor() {
        var quit = new Quit();
        var edit = new Edit();
        var save = new Save();
        COMMANDS.put("quit", quit);
        COMMANDS.put("edit", edit);
        COMMANDS.put("save", save);
        COMMANDS.put("q", quit);
        COMMANDS.put("w", save);
        COMMANDS.put("e", edit);
    }

    public void start() {
        engine.setKeyHandler(new GlobalKeyHandler());
        engine.initGLFW();
        engine.setRenderer(new SkiaRenderer());
        engine.loop();
        engine.destroyGLFW();
    }

    public static void setMode(TextInteractionMode mode) {
        MODE = mode;
    }

    public static TextInteractionMode getMode() {
        return MODE;
    }

    //all commands must prefix be lowercase
    public static void setCommand(String command, ICommand mode) {
        COMMANDS.put(command.toLowerCase(), mode);
    }

    //all commands prefix must be lowercase
    public static ICommand getCommand(String command) {
        return COMMANDS.get(command.toLowerCase());
    }

    // return true if command found and properly executed, otherwise will return false, if the command was found but not properly executed the command instance will return false
    public static boolean executeCommand(String fullCommand) {
        String[] args = fullCommand.split(" ");
        String name = args[0].toLowerCase();
        ICommand executor = getCommand(name);
        if (executor != null) {
            try {
                return executor.run(args);
            } catch (Exception e) {
                System.out.println("Error executing command: " + fullCommand + " -> " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // reset the command buffer
    public static void clearCommandBuffer() {
        commandBuffer.setLength(0);
    }

    public static void main(String[] args) {
        Deditor deditor = new Deditor();
        deditor.start();
    }
}
