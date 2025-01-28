package dev.cwby.commands;

import dev.cwby.BufferManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.TextComponent;

public class VerticalSplit implements ICommand {

    @Override
    public boolean run(String[] args) {
        SkiaRenderer.currentNode.splitHorizontally();
        SkiaRenderer.currentNode.component = null;
        SkiaRenderer.currentNode.leftChild.component = new TextComponent().setBuffer(BufferManager.getActualBuffer());
        SkiaRenderer.currentNode.rightChild.component = new TextComponent().setBuffer(BufferManager.addEmptyBuffer());
        return true;
    }
}
