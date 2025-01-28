package dev.cwby.editor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FileChunkLoader {
    private final File file;
    private final int chunkSize;
    private long currentOffset;

    public FileChunkLoader(File file, int chunkSize) {
        this.file = file;
        this.chunkSize = chunkSize;
        this.currentOffset = 0;
    }

    public List<StringBuilder> loadChunk() {
        List<StringBuilder> chunk = new ArrayList<>();
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(currentOffset);

            String line;
            int linesRead = 0;
            while ((line = raf.readLine()) != null && linesRead < chunkSize) {
                chunk.add(new StringBuilder(line));
                linesRead++;
            }

            currentOffset = raf.getFilePointer();
            raf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return chunk;
    }

    public void resetOffset() {
        currentOffset = 0;
    }

    public boolean hasMoreData() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            return currentOffset < raf.length();
        }
    }
}
