package com.stego;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class StegoService {

    // End of message delimiter to know when to stop reading
    private static final String END_MARKER = "##END##";

    public byte[] encrypt(MultipartFile file, String message) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IOException("Invalid image file");
        }

        String fullMessage = message + END_MARKER;
        byte[] messageBytes = fullMessage.getBytes(StandardCharsets.UTF_8);
        int messageLength = messageBytes.length;
        int messageIndex = 0;
        int bitIndex = 0;

        int width = image.getWidth();
        int height = image.getHeight();

        // Check capacity
        if (messageLength * 8 > width * height) {
            throw new IOException("Message is too long for this image");
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (messageIndex >= messageLength) {
                    break;
                }

                int pixel = image.getRGB(x, y);
                int blue = pixel & 0xFF;

                // Get current bit to encode
                int currentByte = messageBytes[messageIndex];
                int bit = (currentByte >> (7 - bitIndex)) & 1;

                // Modify LSB of blue channel
                blue = (blue & 0xFE) | bit;

                // Update pixel
                int newPixel = (pixel & 0xFFFFFF00) | blue;
                image.setRGB(x, y, newPixel);

                bitIndex++;
                if (bitIndex >= 8) {
                    bitIndex = 0;
                    messageIndex++;
                }
            }
            if (messageIndex >= messageLength) {
                break;
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos); // Always use PNG to avoid compression artifacts
        return baos.toByteArray();
    }

    public String decrypt(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IOException("Invalid image file");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        ByteArrayOutputStream decodedBytes = new ByteArrayOutputStream();
        int currentByte = 0;
        int bitIndex = 0;
        byte[] markerBytes = END_MARKER.getBytes(StandardCharsets.UTF_8);
        byte[] lastBytes = new byte[markerBytes.length];
        int bytesRead = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int blue = pixel & 0xFF; // Get blue channel

                // Extract LSB
                int bit = blue & 1;

                // Build byte
                currentByte = (currentByte << 1) | bit;
                bitIndex++;

                if (bitIndex >= 8) {
                    decodedBytes.write(currentByte);

                    // Sliding window check for end marker
                    if (bytesRead < markerBytes.length) {
                        lastBytes[bytesRead] = (byte) currentByte;
                    } else {
                        System.arraycopy(lastBytes, 1, lastBytes, 0, markerBytes.length - 1);
                        lastBytes[markerBytes.length - 1] = (byte) currentByte;
                    }
                    bytesRead++;

                    if (bytesRead >= markerBytes.length) {
                        boolean match = true;
                        for (int i = 0; i < markerBytes.length; i++) {
                            if (lastBytes[i] != markerBytes[i]) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            byte[] resultBytes = decodedBytes.toByteArray();
                            return new String(resultBytes, 0, resultBytes.length - markerBytes.length,
                                    StandardCharsets.UTF_8);
                        }
                    }

                    currentByte = 0;
                    bitIndex = 0;
                }
            }
        }

        // Return whatever we found if marker not found
        return decodedBytes.toString(StandardCharsets.UTF_8);
    }
}
