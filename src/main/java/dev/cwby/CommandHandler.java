package dev.cwby;

import dev.cwby.commands.Edit;
import dev.cwby.commands.ICommand;
import dev.cwby.commands.Quit;
import dev.cwby.commands.Save;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private static Map<String, ICommand> COMMANDS = new HashMap<>();
    private static StringBuilder buffer = new StringBuilder();

    static {
        var quit = new Quit();
        var edit = new Edit();
        var save = new Save();
        COMMANDS.put("quit", quit);
        COMMANDS.put("edit", edit);
        COMMANDS.put("save", save);
        COMMANDS.put("w", save);
        COMMANDS.put("q", quit);
        COMMANDS.put("e", edit);
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
        buffer.setLength(0);
    }

    public static String getBuffer() {
        return buffer.toString();
    }

    public static StringBuilder getBuilderBuffer() {
        return buffer;
    }

    public static void appendBuffer(char c) {
        buffer.append(c);
    }
}
