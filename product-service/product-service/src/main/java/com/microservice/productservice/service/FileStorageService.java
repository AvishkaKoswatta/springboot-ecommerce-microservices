package com.microservice.productservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath = Paths.get("/app/uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(uploadPath);
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directory");
        }
    }

    public String saveFile(MultipartFile file) {
        try {

            String fileName =
                    UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path path = uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Upload failed");
        }
    }
}