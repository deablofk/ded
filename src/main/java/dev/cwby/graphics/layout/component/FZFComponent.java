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

public class FZFComponent extends FloatingWindow implements IComponent {

    private final Paint paint = new Paint().setColor(0xFFFFFFFF);
    private final List<String> files = findAllFiles(Deditor.getProjectPath());
    private final List<String> results = new ArrayList<>();
    private int index = 0;
    private static final Paint bgPaint = new Paint().setColor(0xFF2E2E2E);
    private static final Paint borderPaint = new Paint().setColor(0xFF000000).setStroke(true).setStrokeWidth(3);
    private static final Paint selectedPaint = new Paint().setColor(0xFF00FF00);

    private String query = "";

    public FZFComponent(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.component = this;
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
        width *= 0.9F;
        height *= 0.9F;
        x = (float) (x + (width * 0.1) / 2);
        y = (float) (y + (height * 0.1) / 2);
        canvas.save();
        Rect rect = Rect.makeXYWH(x, y, width, height);
        canvas.clipRect(rect);
        canvas.drawRect(rect, bgPaint);
        canvas.drawRect(rect, borderPaint);

        float lineHeight = FontManager.getLineHeight();
        for (int i = 0; i < results.size(); i++) {
            String result = results.get(i);
            if (index == i) {
                canvas.drawString(result, x + 10, y + lineHeight + (i * lineHeight), FontManager.getDefaultFont(), selectedPaint);
            } else {
                canvas.drawString(result, x + 10, y + lineHeight + (i * lineHeight), FontManager.getDefaultFont(), paint);
            }
        }

        canvas.restore();
    }

    // return a path to a file
    public String select() {
        String result = results.get(index);
        index = 0;
        hide();
        return result;
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
