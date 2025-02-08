package dev.cwby.graphics.layout;

import dev.cwby.graphics.FontManager;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteWindow extends FloatingWindow {
    private List<CompletionItem> suggestions = new ArrayList<>();
    private int selectedIndex = 0;

    private static final Paint selectedPaint = new Paint().setColor(0xFF00FF00);
    private static final Paint bgPaint = new Paint().setColor(0xFF2E2E2E);
    private static final Paint borderPaint = new Paint().setColor(0xFF000000).setStroke(true).setStrokeWidth(3);
    private static final Paint textPaint = new Paint().setColor(0xFFFFFFFF);
    private static final Font font = FontManager.getDefaultFont();

    public AutoCompleteWindow(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public void setSuggestions(List<CompletionItem> suggestions) {
        this.suggestions = suggestions;
        this.height = suggestions.size() * FontManager.getLineHeight();
        this.visible = !suggestions.isEmpty();
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
            Paint paint = (i == selectedIndex) ? selectedPaint : textPaint;
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
            CompletionItem ci = suggestions.get(selectedIndex);
            selectedIndex = 0;
            return ci;
        }
        return null;
    }

}
