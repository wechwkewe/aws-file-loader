package com.example.bucket.controller;

import com.example.bucket.dto.ImageDTO;
import com.example.bucket.exception.S3FileNotFoundException;
import com.example.bucket.service.ImageLoadService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/")
public class S3ImageController {

    private final ImageLoadService loadService;

    S3ImageController(ImageLoadService loadService) {
        this.loadService = loadService;
    }

    @PostMapping("/uploadImage")
    public String uploadImage(@RequestPart(value = "image") MultipartFile file) {
//       TODO add validation
        return this.loadService.uploadImage(file);
    }

    @GetMapping("/{name}")
    public ResponseEntity<ByteArrayResource> downloadImage(@PathVariable(value = "name") String fileName) {
        final byte[] data = loadService.downloadImage(fileName);
        final ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .contentLength(data.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/random")
    public ResponseEntity<ByteArrayResource> downloadRandomFile() {
        ImageDTO imageDTO = loadService.downloadRandomImage();
        byte[] data = imageDTO.getFileContent();
        final ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageDTO.getFileName() + "\"")
                .body(resource);
    }

    @ExceptionHandler(S3FileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(S3FileNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
