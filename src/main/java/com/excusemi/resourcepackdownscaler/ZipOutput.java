package com.excusemi.resourcepackdownscaler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutput {
    private final ZipOutputStream out;
    private final Path zipPath;

    public ZipOutput(Path zipPath) throws FileNotFoundException {
        out = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
        this.zipPath = zipPath;
    }

    public void addDirectory(String path) throws IOException {
        out.putNextEntry(new ZipEntry(path));
        out.closeEntry();

    }

    public void addFile(String path, byte[] bytes) throws IOException {
        out.putNextEntry(new ZipEntry(path));
        out.write(bytes);
        out.closeEntry();
    }

    public void close() throws IOException {
        out.close();
        System.out.println("Created " +  zipPath);
    }
}
