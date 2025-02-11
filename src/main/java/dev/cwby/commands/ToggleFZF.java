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

        SkiaRenderer.openFloatingWindow(fzfComponent);
        return true;
    }
}
