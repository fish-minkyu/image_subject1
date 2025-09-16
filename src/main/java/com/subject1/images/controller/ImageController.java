package com.subject1.images.controller;

import com.subject1.images.service.ImageService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/projects")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/{projectId}/images")
    public Boolean uploadImg(
        @RequestParam("files") List<MultipartFile> multipartFiles,
        @PathVariable("projectId") Long projectId
    )
        throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        imageService.uploadImg(multipartFiles, projectId);
        return Boolean.TRUE;
    }

    @DeleteMapping("/images/{id}")
    public Boolean deleteImg(@PathVariable("id") Long imageId) {
        imageService.softDeleteImg(imageId);
        return Boolean.TRUE;
    }
}
