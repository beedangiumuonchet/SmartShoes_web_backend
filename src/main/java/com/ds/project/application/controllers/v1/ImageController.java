package com.ds.project.application.controllers.v1;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.URL;
import java.nio.file.*;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    private static final String CACHE_DIR = "src/main/resources/uploads/cache";

    @GetMapping("/{driveId}")
    public ResponseEntity<Resource> getImage(@PathVariable String driveId) {
        try {
            // 1️⃣ Tạo thư mục cache nếu chưa có
            Path cacheDir = Paths.get(CACHE_DIR);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }

            // 2️⃣ Đường dẫn file local cache
            Path localFile = cacheDir.resolve(driveId + ".jpg");

            // 3️⃣ Nếu ảnh đã có sẵn ở local → trả về ngay
            if (Files.exists(localFile)) {
                Resource resource = new FileSystemResource(localFile);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }

            // 4️⃣ Nếu chưa có → tải từ Google Drive
            String driveUrl = "https://drive.google.com/uc?export=download&id=" + driveId;

            try (InputStream in = new URL(driveUrl).openStream()) {
                Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
            }

            Resource resource = new FileSystemResource(localFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}