package dev.cwby.commands;

import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.FZFComponent;

public class ToggleFZF implements ICommand {

    public static FZFComponent fzfComponent = new FZFComponent(0, 0, 400, 400);

    @Override
    public boolean run(String[] args) {
        if (args.length >= 2) {
            String query = args[1];
            fzfComponent.search(query);
        }

        if (fzfComponent.isVisible()) {
            fzfComponent.hide();
            SkiaRenderer.setFloatingWindow(null);
        } else {
            fzfComponent.show(0, 0);
            SkiaRenderer.setFloatingWindow(fzfComponent);
        }
        return true;
    }
}
