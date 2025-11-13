package dev.cwby.commands;

import dev.cwby.graphics.Engine;
import dev.cwby.graphics.OpenGLRenderer;
import dev.cwby.graphics.layout.component.ExplorerWindow;

public class Explorer implements ICommand {
    public static ExplorerWindow explorer = new ExplorerWindow(0, 0, 400, 400);

    @Override
    public boolean run(String[] args) {
        float height = Engine.getHeight();
        float width = Engine.getWidth();
        float x = (float) ((width * 0.1) / 2);
        float y = (float) ((height * 0.1) / 2);

        explorer.show(x, y, width * 0.9F, height * 0.9F);
        OpenGLRenderer.WM.openFloatingWindow(explorer);
        return true;
    }
}
