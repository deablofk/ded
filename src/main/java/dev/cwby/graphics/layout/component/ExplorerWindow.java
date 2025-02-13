package dev.cwby.graphics.layout.component;

import dev.cwby.BufferManager;
import dev.cwby.CommandHandler;
import dev.cwby.Deditor;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.graphics.layout.FloatingWindow;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class ExplorerWindow extends FloatingWindow {

    private ScratchBuffer buffer;
    private Set<PathMatcher> ignoreMatchers;

    public ExplorerWindow(float x, float y, float width, float height) {
        super(x, y, width, height);
        buffer = BufferManager.addEmptyBuffer();
        buffer.lines.clear();
        ignoreMatchers = loadGitignorePatterns(Deditor.getProjectPath());
        ignoreMatchers.add(FileSystems.getDefault().getPathMatcher("glob:**/.*/**"));
        ignoreMatchers.add(FileSystems.getDefault().getPathMatcher("glob:**/*.class"));
        buffer.lines.addAll(findAllFiles(Deditor.getProjectPath()));
        this.component = new TextComponent().setBuffer(buffer);
    }

    private List<StringBuilder> findAllFiles(String projectPath) {
        try {
            return Files.walk(Paths.get(projectPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> !isIgnored(path))
                    .map(path -> new StringBuilder(path.toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Set<PathMatcher> loadGitignorePatterns(String projectPath) {
        Path gitignorePath = Paths.get(projectPath, ".gitignore");
        if (!Files.exists(gitignorePath)) {
            return Collections.emptySet();
        }

        try {
            List<String> patterns = Files.readAllLines(gitignorePath);
            Set<PathMatcher> matchers = patterns.stream()
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .map(line -> {
                        String pattern = "glob:" + projectPath + "/" + line;
                        return FileSystems.getDefault().getPathMatcher(pattern);
                    })
                    .collect(Collectors.toSet());


            return matchers;

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    private boolean isIgnored(Path path) {
        return ignoreMatchers.stream().anyMatch(matcher -> matcher.matches(path));
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
