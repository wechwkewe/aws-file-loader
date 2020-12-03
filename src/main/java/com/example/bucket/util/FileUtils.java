package com.example.bucket.util;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class FileUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static byte[] convertS3ObjectToByteArray(S3Object s3Object) {
        byte[] content = null;

        try (S3ObjectInputStream stream = s3Object.getObjectContent()) {
            content = IOUtils.toByteArray(stream);
        } catch (IOException e) {
            LOG.error("Unable to convert S3Object!", e);
        }
        return content;
    }

    public static File convertMultiPartToFile(MultipartFile multipartFile) {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        } catch (IOException e) {
            LOG.error("Unable to convert MultipartFile!", e);
        }
        return file;
    }

}
