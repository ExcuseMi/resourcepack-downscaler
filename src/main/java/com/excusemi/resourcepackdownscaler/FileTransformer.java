package com.excusemi.resourcepackdownscaler;

import org.imgscalr.Scalr;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

class FileTransformer {

    private final ZipOutput zipOutput;
    private final int size;
    private final boolean updatePackMeta;

    FileTransformer(ZipOutput zipOutput, int size, boolean updatePackMeta) throws IOException {
        this.zipOutput = zipOutput;
        this.size = size;
        this.updatePackMeta = updatePackMeta;
    }

    void appendDirectory(Path path) throws IOException {
        if (path.getFileName() != null) {
            final String name = path.getRoot().relativize(path).toString();
            zipOutput.addDirectory(name);
        }
    }

    void appendFile(Path path, byte[] bytes) throws IOException {
        if (path.getFileName() != null) {
            System.out.println("Copying " + path + " ... ");

            final String name = path.getRoot().relativize(path).toString();
            if (updatePackMeta && "pack.mcmeta".equalsIgnoreCase(name)) {
                final byte[] updatedPack = updatePack(bytes);
                zipOutput.addFile(name, updatedPack);
            } else {
                final String contentType = getContentType(bytes);
                if (!"pack.png".equalsIgnoreCase(name) && contentType != null && contentType.startsWith("image/")) {
                    BufferedImage imageFromBytes = createImageFromBytes(bytes);
                    if(imageFromBytes.getWidth()!= size) {
                        BufferedImage resize = Scalr.resize(imageFromBytes, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, size);
                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            final String formatName = contentType.replace("image/", "");
                            ImageIO.write(resize, formatName, baos);
                            resize = null;
                            byte[] updatedBytes = baos.toByteArray();
                            zipOutput.addFile(name, updatedBytes);
                        }
                    } else {
                        zipOutput.addFile(name, bytes);
                    }
                    imageFromBytes = null;
                } else {
                    zipOutput.addFile(name, bytes);
                }
            }
        }
    }

    private byte[] updatePack(byte[] bytes) {
        final String source = new String(bytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(source);
        final JSONObject pack = jsonObject.optJSONObject("pack");
        if (pack != null) {
            String description = pack.optString("description");
            if (!"".equalsIgnoreCase(description)) {
                description += " - " + size + "x";
                pack.put("description", description);
            }
            jsonObject.put("pack", pack);
        }
        return jsonObject.toString(4).getBytes(StandardCharsets.UTF_8);
    }


    void close() throws IOException {
        zipOutput.close();
    }

    private String getContentType(byte[] array) {
        String contentType = null;
        try {
            contentType = URLConnection.guessContentTypeFromStream(
                    new ByteArrayInputStream(array)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentType;
    }

    private BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
