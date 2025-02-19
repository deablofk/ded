package dev.cwby.graphics.layout.component;

import dev.cwby.BufferManager;
import dev.cwby.editor.ScratchBuffer;
import dev.cwby.graphics.layout.FloatingWindow;
import dev.cwby.pkgs.PackageData;
import dev.cwby.pkgs.PackageManager;

public class PkgManWindow extends FloatingWindow {

    private final ScratchBuffer buffer;

    public PkgManWindow(float x, float y, float width, float height) {
        super(x, y, width, height);
        buffer = BufferManager.addEmptyBuffer();
        buffer.lines.clear();
        this.component = new TextComponent().setBuffer(buffer);
        initializeBuffer();
    }

    public void initializeBuffer() {
        PackageManager.initializePackages();
        for (PackageData packageData : PackageManager.getPackages()) {
            buffer.lines.add(new StringBuilder(packageData.name + " | installed: " + packageData.isInstalled));
        }
    }

    @Override
    public void onTrigger() {
        close();
        String packageName = select().split(" | ")[0];
        PackageManager.installPackage(packageName);
    }

    // return a path to a file
    public String select() {
        hide();
        return buffer.getCurrentLine().toString();
    }

}
