package com.vivatech.mumly_event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileStorageService {

    private final String UPLOAD_DIR = "uploads";

    public String storeFile(MultipartFile file, String fileName, String directory) throws IOException {
        // Ensure the directory exists
        Path dirPath = Paths.get(UPLOAD_DIR + "/" + directory);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // Save the file
        Path filePath = dirPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;

    }

    public String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public void deleteFile(String directory, String fileName) {
        Path path = Paths.get(UPLOAD_DIR + "/" + directory + "/" + fileName);
        try {
            if (Files.exists(path)) {
                Files.delete(path); // Delete the file
            }
        } catch (IOException e) {
            log.error("File not found: {}/{}", directory, fileName, e);
        }
    }
}
