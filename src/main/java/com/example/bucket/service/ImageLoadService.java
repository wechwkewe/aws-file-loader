package com.example.bucket.service;

import com.example.bucket.dto.ImageDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ImageLoadService {
    String uploadImage(MultipartFile multipartFile);

    byte[] downloadImage(String keyName);

    ImageDTO downloadRandomImage();
}
