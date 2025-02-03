package dev.cwby.graphics.layout;

import dev.cwby.graphics.FontManager;
import dev.cwby.graphics.layout.component.IComponent;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;

import java.util.ArrayList;
import java.util.List;

public class FloatingWindow implements IComponent {
    private float x, y, width, height;
    private boolean visible = false;
    private List<CompletionItem> suggestions = new ArrayList<>();
    private int selectedIndex = 0;
    private static final Paint bgPaint = new Paint().setColor(0xFF2E2E2E);
    private static final Paint borderPaint = new Paint().setColor(0xFF000000).setStroke(true).setStrokeWidth(3);
    private static final Paint textPaint = new Paint().setColor(0xFFFFFFFF);
    private static final Font font = FontManager.getDefaultFont();

    public FloatingWindow(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = 0;
    }

    public void setSuggestions(List<CompletionItem> suggestions) {
        this.suggestions = suggestions;
        this.height = suggestions.size() * FontManager.getLineHeight();
        this.visible = !suggestions.isEmpty();
    }

    public void show(float x, float y) {
        this.x = x;
        this.y = y;
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    @Override
    public void render(Canvas canvas, float offsetX, float offsetY, float parentWidth, float parentHeight) {
        if (!visible) return;

        canvas.save();
        Rect rect = Rect.makeXYWH(x, y, width, height);
        canvas.clipRect(rect);
        canvas.drawRect(rect, bgPaint);
        canvas.drawRect(rect, borderPaint);

        float textY = y + FontManager.getLineHeight();
        for (int i = 0; i < suggestions.size(); i++) {
            CompletionItem item = suggestions.get(i);
            Paint paint = (i == selectedIndex) ? new Paint().setColor(0xFF00FF00) : textPaint;
            canvas.drawString(item.getLabel(), x + 5, textY, font, paint);
            textY += FontManager.getLineHeight();
        }

        canvas.restore();
    }

    public void moveSelection(int delta) {
        if (!visible) return;
        selectedIndex = Math.max(0, Math.min(selectedIndex + delta, suggestions.size() - 1));
    }

    public CompletionItem select() {
        if (visible && !suggestions.isEmpty()) {
            hide();
            return suggestions.get(selectedIndex);
        }
        return null;
    }

    public boolean isVisible() {
        return visible;
    }
}