package dev.cwby.editor;

import dev.cwby.FileUtils;

import java.io.File;

public class TextBuffer extends ScratchBuffer {
    public FileChunkLoader fileChunkLoader;
    public File file;

    public TextBuffer() {
        this.lines.add(new StringBuilder());
    }

    public TextBuffer(FileChunkLoader loader) {
        lines.clear();
        fileChunkLoader = loader;
        loadInitialChunk();
        this.file = loader.getFile();
        this.filepath = file.getAbsolutePath();
        this.fileType = FileUtils.getFileExtension(file.getAbsolutePath());
    }

    private void loadInitialChunk() {
        lines.addAll(fileChunkLoader.loadChunkByByteSize());
    }

}
