package dev.cwby;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public final class FileUtils {

    public static File findProjectRoot(File file, List<String> rootIdentifiers) {
        File dir = file.isDirectory() ? file : file.getParentFile();
        while (dir != null) {
            for (String indicator : rootIdentifiers) {
                if (Files.exists(dir.toPath().resolve(indicator))) {
                    return dir;
                }
            }
            dir = dir.getParentFile();
        }

        return file;
    }

    public static String getFileExtension(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1) {
            return "";
        }

        return fileName.substring(dotIndex + 1);
    }
}
