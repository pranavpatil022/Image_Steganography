package com.stego;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class StegoController {

    @Autowired
    private StegoService stegoService;

    @PostMapping("/encrypt")
    public ResponseEntity<ByteArrayResource> encrypt(
            @RequestParam("image") MultipartFile image,
            @RequestParam("message") String message) {
        try {
            byte[] processedImage = stegoService.encrypt(image, message);
            ByteArrayResource resource = new ByteArrayResource(processedImage);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"secret_image.png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .contentLength(processedImage.length)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<String> decrypt(@RequestParam("image") MultipartFile image) {
        try {
            String message = stegoService.decrypt(image);
            return ResponseEntity.ok(message);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to decrypt image");
        }
    }
}
