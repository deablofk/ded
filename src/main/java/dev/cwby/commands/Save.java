package dev.cwby.commands;

import dev.cwby.Deditor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Save implements ICommand {
    @Override
    public boolean run(String[] args) {
        File file = Deditor.buffer.file;
        if (file != null) {
            List<String> lines = Deditor.buffer.getLines();
            try {
                Files.write(file.toPath(), lines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        return true;
    }
}
