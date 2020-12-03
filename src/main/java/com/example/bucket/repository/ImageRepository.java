package com.example.bucket.repository;

import com.example.bucket.entity.Image;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ImageRepository extends CrudRepository<Image, Long> {

    @Query(value = "SELECT image_name FROM image ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    String findRandomImageName();
}
