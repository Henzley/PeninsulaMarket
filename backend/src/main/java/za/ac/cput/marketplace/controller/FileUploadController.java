package za.ac.cput.marketplace.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final String uploadDir = "uploads/";

    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        // Make sure the uploads folder exists
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create a unique file name so different uploads never overwrite each other
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + extension;

        // Save the file to disk
        Path path = Paths.get(uploadDir + newFileName);
        Files.write(path, file.getBytes());

        // Return the public URL for this image
        return "http://localhost:8080/" + newFileName;
    }
}