package com.subject1.images.service;

import com.subject1.images.entity.Image;
import com.subject1.images.repo.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@Slf4j
public class ThumbnailService {
    private final ImageRepository imageRepository;
    private final MinioService minioService;

    public ThumbnailService(ImageRepository imageRepository, MinioService minioService) {
        this.imageRepository = imageRepository;
        this.minioService = minioService;
    }

    @Value("${minio.url}")
    private String minio;

    @Async
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2) // 지수 백오프: 1초, 2초, 4초
    )
    public void generateThumbnail(Long imageId) {
        log.info("Started thumbnail generation for Image ID: {}", imageId);
        // 해당 이미지를 DB에서 찾는다.
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        try {
            // 썸네일 생성 상태를 PROCESSING으로 변경한다.
            image.setThumbnailStatus(Image.ThumbnailStatus.PROCESSING);
            imageRepository.save(image);

            // 원본 파일 이미지를 다운로드한다.
            String storedFileName = image.getStoredFileName();
            String thumbnailFileName = "thumbnail_" + storedFileName;
            try (
                InputStream originalImageStream = minioService.getFileInputStream(storedFileName);
                ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
            ) {
                // 썸네일 생성
                Thumbnails.of(originalImageStream)
                    .size(150, 150)
                    .toOutputStream(thumbnailOutputStream);

                byte[] thumbnailBytes = thumbnailOutputStream.toByteArray();

                // 썸네일을 MinIO에 저장한다.
                InputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailBytes);
                minioService.saveThumbnailFile(thumbnailFileName, thumbnailInputStream, thumbnailBytes.length);

                // 썸네일 URL을 생성하고, DB 업데이트한다.
                String thumbnailUrl = minio + "/" + storedFileName;
                image.setThumbnailUrl(thumbnailUrl);
                image.setThumbnailStatus(Image.ThumbnailStatus.READY);
                imageRepository.save(image);

                log.info("Thumbnail generation completed for Image ID: {}", imageId);
            }
        } catch (Exception e) {
            log.error("Thumbnail generation failed for Image ID: {} - {}", imageId, e.getMessage());
            // 예외 발생 시, 썸네일 상태 FAILED 변경 후 예외 재발생 -> @Retryable가 재시도 처리
            image.setThumbnailStatus(Image.ThumbnailStatus.FAILED);
            imageRepository.save(image);
            throw new RuntimeException(e);
        }
    }
}
