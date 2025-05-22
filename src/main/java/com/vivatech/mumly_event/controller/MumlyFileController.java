package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@RestController
@RequestMapping("/api/event/files")
public class MumlyFileController {

    private final String UPLOAD_DIR = "uploads";

    @GetMapping("/download-file")
    public Response downloadFile(@RequestParam String filePath) {
        Response responseDto = new Response();
        try {
            // Get the file bytes
            Path fullPath = Paths.get(UPLOAD_DIR + "/" + filePath);

            // Read the file from the given file path
            byte[] fileBytes = Files.readAllBytes(fullPath);

            // Detect the file type (MIME type)
            String mimeType = Files.probeContentType(fullPath);
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // Fallback if MIME type is unknown
            }

            // Convert file bytes to Base64 format
            String base64File = Base64.getEncoder().encodeToString(fileBytes);

            // Combine MIME type with Base64 data
            String dataWithMimeType = "data:" + mimeType + ";base64," + base64File;

            // Return the Base64 encoded file
            responseDto.setStatus("SUCCESS");
            responseDto.setMessage("File Found");
            responseDto.setData(dataWithMimeType);

        } catch (Exception e) {
            responseDto.setStatus("FAILED");
            responseDto.setMessage("File not found!");
        }
        return responseDto;
    }
}
