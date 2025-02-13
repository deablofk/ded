package dev.cwby.graphics.layout;

import dev.cwby.BufferManager;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.graphics.layout.component.TextComponent;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AutoCompleteWindow extends FloatingWindow {
    private List<CompletionItem> suggestions = new ArrayList<>();
    public ScratchBuffer buffer;

    public AutoCompleteWindow(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.component = new TextComponent().setBuffer(BufferManager.addEmptyBuffer());
        buffer = ((TextComponent) this.getComponent()).getBuffer();
        visible = false;
    }

    public void setSuggestions(List<CompletionItem> suggestions) {
        this.suggestions = suggestions;
        this.visible = !suggestions.isEmpty();
        buffer.setLines(suggestions.stream().map(x -> new StringBuilder(x.getLabel())).collect(Collectors.toList()));
        buffer.setVisibleLines(getVisibleLines());
        buffer.cursorY = 0;
    }

    public CompletionItem select() {
        if (visible && !suggestions.isEmpty()) {
            hide();
            CompletionItem ci = suggestions.get(buffer.cursorY);
            buffer.cursorY = 0;
            return ci;
        }
        return null;
    }

}
