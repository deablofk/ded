package dev.cwby.graphics.layout;

import dev.cwby.graphics.SkiaRenderer;
import dev.cwby.graphics.layout.component.IComponent;
import dev.cwby.graphics.layout.component.SplitType;

public class WindowNode {
    public float x, y, width, height;
    public IComponent component;
    public WindowNode leftChild, rightChild;
    public SplitType splitType = SplitType.NONE;
    public WindowNode father;

    public WindowNode(float x, float y, float width, float height, WindowNode father) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.father = father;
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

        leftChild = new WindowNode(x, y, leftWidth, height, this);
        rightChild = new WindowNode(x + leftWidth, y, width - leftWidth, height, this);
        splitType = SplitType.VERTICAL;
        updateSize(x, y, width, height);
    }

    public void splitHorizontally(float topHeight) {
        if (topHeight <= 0 || topHeight >= height) {
            throw new IllegalArgumentException("Top height must be between 0 and the total height.");
        }

        leftChild = new WindowNode(x, y, width, topHeight, this);
        rightChild = new WindowNode(x, y + topHeight, width, height - topHeight, this);
        splitType = SplitType.HORIZONTAL;
        updateSize(x, y, width, height);
    }

    public void updateSize(float newX, float newY, float newWidth, float newHeight) {
        x = newX;
        y = newY;
        width = newWidth;
        height = newHeight;

        if (!isLeaf()) {
            float halfWidth = width / 2.0f;
            float halfHeight = height / 2.0f;
            if (splitType == SplitType.VERTICAL) {
                leftChild.updateSize(x, y, halfWidth, height);
                rightChild.updateSize(x + halfWidth, y, halfWidth, height);
            } else if (splitType == SplitType.HORIZONTAL) {
                leftChild.updateSize(x, y, width, halfHeight);
                rightChild.updateSize(x, y + halfHeight, width, halfHeight);
            }
        }
    }

    public void printTree(String prefix) {
        System.out.println(prefix + "RegionNode: [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height +
                ", splitType=" + splitType + ", isLeaf=" + isLeaf() + "]");

        if (leftChild != null) {
            leftChild.printTree(prefix + (splitType == SplitType.HORIZONTAL ? "  T-> " : " L-> "));
        }

        if (rightChild != null) {
            rightChild.printTree(prefix + (splitType == SplitType.HORIZONTAL ? "  D-> " : " R-> "));
        }
    }

    private WindowNode findNeighbor(int direction) {
        WindowNode node = this;
        while (node.father != null) {
            WindowNode sibling = (node == node.father.leftChild) ? node.father.rightChild : node.father.leftChild;
            if (sibling != null) {
                if (direction == 0 && sibling.x + sibling.width == node.x) return findLeaf(sibling); // Left
                if (direction == 1 && sibling.x == node.x + node.width) return findLeaf(sibling); // Right
                if (direction == 2 && sibling.y + sibling.height == node.y) return findLeaf(sibling); // Up
                if (direction == 3 && sibling.y == node.y + node.height) return findLeaf(sibling); // Down
            }
            node = node.father;
        }
        return null;
    }

    private WindowNode findLeaf(WindowNode node) {
        while (!node.isLeaf()) {
            WindowNode closerChild;
            WindowNode fartherChild;

            float leftDistance = (float) Math.sqrt(Math.pow(node.leftChild.x - this.x, 2) + Math.pow(node.leftChild.y - this.y, 2));
            float rightDistance = (float) Math.sqrt(Math.pow(node.rightChild.x - this.x, 2) + Math.pow(node.rightChild.y - this.y, 2));

            if (leftDistance < rightDistance) {
                closerChild = node.leftChild;
                fartherChild = node.rightChild;
            } else {
                closerChild = node.rightChild;
                fartherChild = node.leftChild;
            }

            boolean xAligned = (this.x >= closerChild.x && this.x < closerChild.x + closerChild.width) || (closerChild.x >= this.x && closerChild.x < this.x + this.width);
            boolean yAligned = (this.y >= closerChild.y && this.y < closerChild.y + closerChild.height) || (closerChild.y >= this.y && closerChild.y < this.y + this.height);

            node = (xAligned || yAligned) ? closerChild : fartherChild;
        }
        return node;
    }

    public WindowNode moveLeft() {
        WindowNode neighbor = findNeighbor(0);
        if (neighbor != null) SkiaRenderer.currentNode = neighbor;
        return neighbor;
    }

    public WindowNode moveRight() {
        WindowNode neighbor = findNeighbor(1);
        if (neighbor != null) SkiaRenderer.currentNode = neighbor;
        return neighbor;
    }

    public WindowNode moveUp() {
        WindowNode neighbor = findNeighbor(2);
        if (neighbor != null) SkiaRenderer.currentNode = neighbor;
        return neighbor;
    }

    public WindowNode moveDown() {
        WindowNode neighbor = findNeighbor(3);
        if (neighbor != null) SkiaRenderer.currentNode = neighbor;
        return neighbor;
    }
}
