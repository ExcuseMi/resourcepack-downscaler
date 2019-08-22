package com.excusemi.resourcepackdownscaler;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

class ZipVisitor {

    static void visitZipFiles(Path fromZip, FileTransformer fileTransformer) throws IOException {
        try {
            for (Path root : FileSystems.newFileSystem(fromZip, ZipVisitor.class.getClassLoader())
                    .getRootDirectories()) {
                try (Stream<Path> paths = Files.walk(root)) {
                    paths
                            .forEach(path -> {
                                if (Files.isRegularFile(path)) {
                                    final byte[] bytes;
                                    try {
                                        bytes = Files.readAllBytes(path);
                                        fileTransformer.appendFile(path, bytes);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else if (Files.isDirectory(path)) {
                                    try {
                                        fileTransformer.appendDirectory(path);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                fileTransformer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
