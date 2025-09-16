package com.subject1.images.service;

import com.subject1.images.entity.Image;
import com.subject1.images.repo.ImageRepository;
import com.subject1.images.util.HashGenerator;
import com.subject1.images.util.ImageAlreadyExistsException;
import io.minio.errors.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;
    private final MinioService minioService;
    private final ThumbnailService thumbnailService;

    public ImageService(ImageRepository imageRepository, MinioService minioService, ThumbnailService thumbnailService) {
        this.imageRepository = imageRepository;
        this.minioService = minioService;
        this.thumbnailService = thumbnailService;
    }

    @Value("${minio.url}")
    private String minio;

    @Transactional
    public void uploadImg(List<MultipartFile> multipartFiles, Long projectId)
        throws IOException,
            NoSuchAlgorithmException,
            ServerException,
            InsufficientDataException,
            ErrorResponseException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        for (MultipartFile multipartFile : multipartFiles) {
            // 1. 이미지 파일의 해시 값 계산
            String fileHash;
            try (InputStream inputStream = multipartFile.getInputStream()) {
                // 해시값 초기화
                fileHash = HashGenerator.calculateSha256Hash(inputStream);

                // 2. 해시 값으로 DB에서 기존 이미지 조회(FOR UPDATE 락 적용)
                Optional<Image> existedImage = imageRepository.findForUpdateByHashValue(fileHash);
                // 3. 이미지가 이미 존재한다면,
                if (existedImage.isPresent()) {
                    // 예외처리를 한다.
                    throw new ImageAlreadyExistsException("Image already exists");
                }

                // 4.이미지가 중복되지 않다면
                String storedFileName = UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
                // MinIO에 해당 파일을 저장한다.
                minioService.saveImgFile(storedFileName, multipartFile);

                String minioUrl = minio + storedFileName;

                Image newImage = Image.builder()
                    .projectId(projectId)
                    .fileName(multipartFile.getOriginalFilename())
                    .storedFileName(storedFileName)
                    .fileUrl(minioUrl)
                    .hashValue(fileHash)
                    .build();

                // 5. DB에 image의 메타 데이터 저장한다.
                imageRepository.save(newImage);

                // 6. 비동기로 썸네일 생성 트리거
                thumbnailService.generateThumbnail(newImage.getImageId());
            } catch (IOException e) {
                throw new IOException(e.getMessage());
            } catch (Exception e) {
                log.error("Exception err: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "uploadImg Method");
            }
        }
    }
}
