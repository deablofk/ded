package dev.cwby.editor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
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

    public List<StringBuilder> loadChunckByLineOffset() {
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

    public List<StringBuilder> loadChunkByByteSize() {
        List<StringBuilder> chunk = new ArrayList<>();
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(currentOffset);

            byte[] buffer = new byte[(int) chunkSize];
            int bytesRead = raf.read(buffer);

            // EOF
            if (bytesRead == -1) {
                return chunk;
            }

            String partialData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            String[] lines = partialData.split("\n");
            for (int i = 0; i < lines.length; i++) {
                if (i == lines.length - 1 && bytesRead == chunkSize) {
                    currentOffset += lines[i].getBytes(StandardCharsets.UTF_8).length;
                    break;
                } else {
                    chunk.add(new StringBuilder(lines[i]));
                }
            }
            currentOffset += bytesRead;
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

    public File getFile() {
        return file;
    }
}
