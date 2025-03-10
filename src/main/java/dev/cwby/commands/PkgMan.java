package dev.cwby.commands;

import dev.cwby.graphics.Engine;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.PkgManWindow;

public class PkgMan implements ICommand {
    private final PkgManWindow pkgManWindow = new PkgManWindow(0, 0, 400, 400);

    @Override
    public boolean run(String[] args) {
        float height = Engine.getHeight();
        float width = Engine.getWidth();
        float x = (float) ((width * 0.1) / 2);
        float y = (float) ((height * 0.1) / 2);

        pkgManWindow.show(x, y, width * 0.9F, height * 0.9F);
        SkiaRenderer.WM.openFloatingWindow(pkgManWindow);
        return true;
    }
}
