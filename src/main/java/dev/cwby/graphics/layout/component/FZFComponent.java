package dev.cwby.graphics.layout.component;

import dev.cwby.Deditor;
import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.layout.FloatingWindow;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FZFComponent extends FloatingWindow {

    private final Paint paint = new Paint().setColor(0xFFFFFFFF);
    // this reads all files on creating a new FZF Component Instance
    private final List<String> files = findAllFiles(Deditor.getProjectPath());
    private final List<String> results = new ArrayList<>();
    private int index = 0;
    private static final Paint bgPaint = new Paint().setColor(0xFF2E2E2E);
    private static final Paint borderPaint = new Paint().setColor(0xFF000000).setStroke(true).setStrokeWidth(3);

    private String query = "";

    // initial
    public FZFComponent(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public List<String> getCachedFiles() {
        return files;
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
        this.results.clear();
        for (String file : files) {
            if (fuzzyMatch(query, file)) {
                results.add(file);
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
    public void render(Canvas canvas, float x, float y, float width, float height) {
        width *= 0.9;
        height *= 0.9;
        x = (float) (x + (width * 0.1) / 2);
        y = (float) (y + (height * 0.1) / 2);
        canvas.save();
        Rect rect = Rect.makeXYWH(x, y, width, height);
        canvas.clipRect(rect);
        canvas.drawRect(rect, bgPaint);
        canvas.drawRect(rect, borderPaint);

        float currentY = y + FontManager.getLineHeight();
        for (String result : results) {
            canvas.drawString(result, x + 10, currentY, FontManager.getDefaultFont(), paint);
            currentY += FontManager.getLineHeight();
        }

        canvas.restore();
    }

    // return a path to a file
    public String select() {
        return results.get(index);
    }

    public void prev() {
        if (index > 0) {
            index--;
        }
    }

    public void next() {
        if (index < results.size() - 1) {
            index++;
        }
    }
}
