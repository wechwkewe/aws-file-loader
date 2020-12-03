package com.example.bucket.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.example.bucket.dto.ImageDTO;
import com.example.bucket.entity.Image;
import com.example.bucket.exception.S3FileNotFoundException;
import com.example.bucket.repository.ImageRepository;
import com.example.bucket.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Objects;
import java.util.UUID;

@Service
public class S3ImageLoadService implements ImageLoadService {

    private static final Logger LOG = LoggerFactory.getLogger(S3ImageLoadService.class);

    private AmazonS3 s3client;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    final ImageRepository fileRepository;

    public S3ImageLoadService(ImageRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }

    @Override
    public String uploadImage(MultipartFile multipartFile) {
        Objects.requireNonNull(multipartFile);
        String fileUrl = null;
        try {
            File file = FileUtils.convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile.getOriginalFilename());
            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            uploadToS3(fileName, file);
            fileRepository.save(new Image(fileName));
            file.delete();
        } catch (Exception e) {
            LOG.error("Unable to upload file!", e);
        }
        return fileUrl;
    }

    private void uploadToS3(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    @Override
    public byte[] downloadImage(final String keyName) {
        try {
            S3Object s3Object = s3client.getObject(bucketName, keyName);
            return FileUtils.convertS3ObjectToByteArray(s3Object);
        } catch (AmazonS3Exception e) {
            LOG.error("No image found", e);
            throw new S3FileNotFoundException();
        }
    }

    @Override
    public ImageDTO downloadRandomImage() {
        String imageName = fileRepository.findRandomImageName();
        if (imageName == null) {
            throw new S3FileNotFoundException();
        }
        S3Object s3Object = s3client.getObject(bucketName, imageName);
        byte[] content = FileUtils.convertS3ObjectToByteArray(s3Object);

        return new ImageDTO(content, s3Object.getKey());
    }

    private String generateFileName(String originalFilename) {
        return UUID.randomUUID().toString() + originalFilename;
    }
}
