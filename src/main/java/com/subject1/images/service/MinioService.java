package com.subject1.images.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class MinioService {
    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Value("${minio.bucket.name}")
    private String BUCKET_NAME;

    @Value("${minio.dir.image}")
    private String IMAGE_DIR;

    @Transactional
    public String saveImgFile(String storedFileName, MultipartFile multipartFile)
        throws IOException,
        ServerException,
        InsufficientDataException,
        ErrorResponseException,
        NoSuchAlgorithmException,
        InvalidKeyException,
        InvalidResponseException,
        XmlParserException,
        InternalException {
        // 환경변수로 설정한 버킷이 존재한지 확인한다.
        boolean isExist = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(BUCKET_NAME)
                .build());
        // 존재하지 않다면, 새로 생성한다.
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket(BUCKET_NAME)
                .build());
        }
        // MinIO에 저장한다.
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(storedFileName)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                .contentType(multipartFile.getContentType())
                .build());
        // MinIO의 저장된 경로를 반환한다.
        return "/" + storedFileName;
    }
}
