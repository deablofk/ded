package dev.cwby.pkgs.sources;

import dev.cwby.pkgs.PackageData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;

import static dev.cwby.pkgs.PackageManager.INTERNALS_DIR;

public class GenericSourceInstaller implements ISourceInstaller {

    @Override
    public void install(PackageData packageData) {
        File file = new File(INTERNALS_DIR + packageData.name);
        file.mkdirs();
        try {
            URL url = new URL(packageData.source.packageOrUrl);
            String fileName = extractFileNameFromUrl(url);
            String filepath = file.getPath() + "/" + fileName;
            downloadPackage(url, filepath);
            extractTarGz(filepath, fileName);
            removePackageFile(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractFileNameFromUrl(URL url) {
        return Paths.get(url.getPath()).getFileName().toString();
    }

    private void downloadPackage(URL url, String filePath) throws IOException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileOutputStream.close();
    }

    private void extractTarGz(String filePath, String fileToExtract) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("tar", "-xf", fileToExtract);
        processBuilder.directory(new File(filePath).getParentFile());
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Extraction failed with exit code: " + exitCode);
            } else {
                System.out.println("Extraction successful.");
                removePackageFile(fileToExtract);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Extraction was interrupted.", e);
        }
    }


    private void removePackageFile(String filepath) {
        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
        }
    }
}
