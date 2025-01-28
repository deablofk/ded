package dev.cwby.graphics.layout;

import dev.cwby.graphics.layout.component.IComponent;

public class RegionNode {
    public float x, y, width, height;
    public IComponent component;
    public RegionNode leftChild, rightChild;

    public RegionNode(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isLeaf() {
        return leftChild == null && rightChild == null;
    }

    public void splitVertically() {
        splitVertically(2.0F);
    }

    public void splitHorizontally() {
        splitHorizontally(2.0F);
    }

    public void splitVertically(float leftWidth) {
        if (leftWidth <= 0 || leftWidth >= width) {
            throw new IllegalArgumentException("Left width must be between 0 and the total width.");
        }

        leftChild = new RegionNode(x, y, leftWidth, height);
        rightChild = new RegionNode(x + leftWidth, y, width - leftWidth, height);
    }

    public void splitHorizontally(float topHeight) {
        if (topHeight <= 0 || topHeight >= height) {
            throw new IllegalArgumentException("Top height must be between 0 and the total height.");
        }

        leftChild = new RegionNode(x, y, width, topHeight);
        rightChild = new RegionNode(x, y + topHeight, width, height - topHeight);
    }

    public void updateSize(float newX, float newY, float newWidth, float newHeight) {
        x = newX;
        y = newY;
        width = newWidth;
        height = newHeight;

        if (!isLeaf()) {
            float halfWidth = width / 2.0f;
            leftChild.updateSize(x, y, halfWidth, height);
            rightChild.updateSize(x + halfWidth, y, halfWidth, height);
        }
    }
}
