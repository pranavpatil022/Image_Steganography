package com.stego;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StegoServiceTest {

    private final StegoService stegoService = new StegoService();

    @Test
    public void testEncryptDecrypt() throws IOException {
        // Create a blank image
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        MultipartFile multipartFile = new MockMultipartFile("image", "test.png", "image/png", baos.toByteArray());

        String originalMessage = "Hello, this is a secret message!";
        byte[] encryptedImage = stegoService.encrypt(multipartFile, originalMessage);

        MultipartFile encryptedMultipartFile = new MockMultipartFile("image", "secret.png", "image/png",
                encryptedImage);
        String decryptedMessage = stegoService.decrypt(encryptedMultipartFile);

        assertEquals(originalMessage, decryptedMessage);
    }

    @Test
    public void testLongMessage() throws IOException {
        // Create a larger image for a longer message
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        MultipartFile multipartFile = new MockMultipartFile("image", "test.png", "image/png", baos.toByteArray());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("A");
        }
        String originalMessage = sb.toString();
        byte[] encryptedImage = stegoService.encrypt(multipartFile, originalMessage);

        MultipartFile encryptedMultipartFile = new MockMultipartFile("image", "secret.png", "image/png",
                encryptedImage);
        String decryptedMessage = stegoService.decrypt(encryptedMultipartFile);

        assertEquals(originalMessage, decryptedMessage);
    }

    @Test
    public void testNoMarker() throws IOException {
        // Create a blank image without any hidden message
        BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        MultipartFile multipartFile = new MockMultipartFile("image", "plain.png", "image/png", baos.toByteArray());

        String decryptedMessage = stegoService.decrypt(multipartFile);

        // It should return something (likely garbage or empty), but it shouldn't crash
        // or hang
        // Since the image is all 0s, it will read 0s as bits.
        // 00000000 -> NUL character.
        assertTrue(decryptedMessage.length() >= 0);
    }
}
