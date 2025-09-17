package com.subject1.images.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinioService {
    private final MinioClient minioClient;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Value("${minio.bucket.image.name}")
    private String IMAGE_BUCKET_NAME;

    @Value("${minio.bucket.thumbnail.name}")
    private String THUMBNAIL_BUCKET_NAME;

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
                .bucket(IMAGE_BUCKET_NAME)
                .build());
        // 존재하지 않다면, 새로 생성한다.
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket(IMAGE_BUCKET_NAME)
                .build());
        }
        // MinIO에 저장한다.
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(IMAGE_BUCKET_NAME)
                .object(storedFileName)
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                .contentType(multipartFile.getContentType())
                .build());
        // MinIO의 저장된 경로를 반환한다.
        return "/" + storedFileName;
    }

    public String saveThumbnailFile(String storedFileName, InputStream inputStream, long size)
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
                .bucket(THUMBNAIL_BUCKET_NAME)
                .build());
        // 존재하지 않다면, 새로 생성한다.
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket(THUMBNAIL_BUCKET_NAME)
                .build());
        }
        // MinIO에 저장한다.
        minioClient.putObject(PutObjectArgs.builder()
            .bucket(THUMBNAIL_BUCKET_NAME)
            .object(storedFileName)
            .stream(inputStream, size, -1)
            .contentType("image/jpeg")
            .build());

        // MinIO의 저장된 경로를 반환한다.
        return "/" + storedFileName;
    }

    // MinIO에서 저장된 파일을 InputStream으로 반환한다.
    public InputStream getFileInputStream(String storedFileName)
        throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(IMAGE_BUCKET_NAME)
                .object(storedFileName)
                .build()
        );
    }

    // 저장된 파일을 완전한 URL로 반환한다.
    public String getPresignedUrl(String storedFileName)
        throws ServerException,
        InsufficientDataException,
        ErrorResponseException,
        IOException,
        NoSuchAlgorithmException,
        InvalidKeyException,
        InvalidResponseException,
        XmlParserException,
        InternalException {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET) // GET 메서드로 접근 허용
                .bucket(IMAGE_BUCKET_NAME)
                .object(storedFileName)
                .expiry(3600, TimeUnit.SECONDS) // 유효기간 설정
                .build());
    }
}
