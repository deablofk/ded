package dev.cwby.commands;

import dev.cwby.BufferManager;
import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.WindowNode;
import dev.cwby.graphics.layout.component.TextComponent;

public class VerticalSplit implements ICommand {

    @Override
    public boolean run(String[] args) {
        WindowNode currentNode = SkiaRenderer.currentNode;
        currentNode.splitVertically();
        if (currentNode.component != null) {
            currentNode.leftChild.component = currentNode.component;
            currentNode.rightChild.component = currentNode.component;
        } else {
            TextComponent component = new TextComponent().setBuffer(BufferManager.addEmptyBuffer());
            currentNode.leftChild.component = component;
            currentNode.rightChild.component = component;
        }
        SkiaRenderer.currentNode = currentNode.rightChild;
        return true;
    }
}
