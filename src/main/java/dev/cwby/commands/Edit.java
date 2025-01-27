package dev.cwby.commands;


import dev.cwby.Deditor;
import dev.cwby.editor.TextBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Edit implements ICommand {

    @Override
    public boolean run(String[] args) {
        if (args.length >= 2) {
            System.out.println(args[1]);
            File file = new File(args[1]);
            if (file.exists()) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    System.out.println(lines);
                    Deditor.buffer = new TextBuffer(lines);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
