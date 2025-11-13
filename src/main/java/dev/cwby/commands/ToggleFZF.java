package dev.cwby.commands;

import dev.cwby.graphics.Engine;
import dev.cwby.graphics.OpenGLRenderer;
import dev.cwby.graphics.layout.component.FZFWindow;

public class ToggleFZF implements ICommand {

    public static FZFWindow fzfComponent = new FZFWindow(0, 0, 400, 400);

    @Override
    public boolean run(String[] args) {
        if (args.length >= 2) {
            String query = args[1];
            fzfComponent.search(query);
        }
        float height = Engine.getHeight();
        float width = Engine.getWidth();
        float x = (float) ((width * 0.1) / 2);
        float y = (float) ((height * 0.1) / 2);

        fzfComponent.show(x, y, width * 0.9F, height * 0.9F);
        OpenGLRenderer.WM.openFloatingWindow(fzfComponent);
        return true;
    }
}
