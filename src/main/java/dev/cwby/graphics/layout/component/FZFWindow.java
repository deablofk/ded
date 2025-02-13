package dev.cwby.graphics.layout.component;

import dev.cwby.BufferManager;
import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.graphics.layout.FloatingWindow;
import io.github.humbleui.skija.Paint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FZFWindow extends FloatingWindow {

    private final Paint paint = new Paint().setColor(0xFFFFFFFF);
    private final List<String> files = findAllFiles(Deditor.getProjectPath());
    private String query = "";

    private ScratchBuffer buffer;

    public FZFWindow(float x, float y, float width, float height) {
        super(x, y, width, height);
        buffer = BufferManager.addEmptyBuffer();
        this.component = new TextComponent().setBuffer(buffer);
    }

    private List<String> findAllFiles(String projectPath) {
        try {
            return Files.walk(Paths.get(projectPath)).filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void search(String query) {
        this.query = query;
        this.buffer.lines.clear();
        for (String file : files) {
            if (fuzzyMatch(query, file)) {
                this.buffer.lines.add(new StringBuilder(file));
            }
        }
    }

    public boolean fuzzyMatch(String query, String target) {
        int queryIndex = 0;
        for (char c : target.toCharArray()) {
            if (queryIndex < query.length() && c == query.charAt(queryIndex)) {
                queryIndex++;
            }
        }

        return queryIndex == query.length();
    }

    @Override
    public void onTrigger() {
        close();
        CommandHandler.executeCommand("edit " + select());
    }

    // return a path to a file
    public String select() {
        hide();
        return buffer.getCurrentLine().toString();
    }

}
