package dev.cwby.commands;

import dev.cwby.BufferManager;
import dev.cwby.graphics.OpenGLRenderer;
import dev.cwby.graphics.layout.component.TextComponent;

import java.io.File;

public class Edit implements ICommand {

    @Override
    public boolean run(String[] args) {
        if (args.length < 2) {
            System.out.println("Specify the File Path");
            return false;
        }
        OpenGLRenderer.WM.getCurrentWindow().component = new TextComponent().setBuffer(BufferManager.openFileBuffer(args[1]));
        return true;
    }

}
