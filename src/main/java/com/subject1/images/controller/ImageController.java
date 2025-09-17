package com.subject1.images.controller;

import com.subject1.images.dto.ImageCursorPageDto;
import com.subject1.images.dto.SearchParam;
import com.subject1.images.entity.Image;
import com.subject1.images.service.ImageService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@Slf4j
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // 이미지 업로드 API
    @PostMapping("/projects/{projectId}/images")
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

    // 이미지 목록 조회 API
    @GetMapping("/projects/{projectId}/images")
    public ResponseEntity<?> getListImg(
        @PathVariable Long projectId,
        @ModelAttribute SearchParam searchParam,
        @PageableDefault(size = 10) Pageable pageable) {
        // Offset 방식
//        Page<Image> offsetPage = imageService.getListImgListOffset(searchParam, pageable);
//        return ResponseEntity.ok(offsetPage);

        // Cursor 방식
        int pageSize = pageable.getPageSize() > 0 ? pageable.getPageSize() : 10;
        ImageCursorPageDto cursorPage = imageService.getListImgListCursor(searchParam, pageSize);
        return ResponseEntity.ok(cursorPage);
    }

    // 이미지 단건 조회
    @GetMapping("/images/{id}")
    public Image getImage(@PathVariable("id") Long imageId)
        throws ServerException,
        InsufficientDataException,
        ErrorResponseException,
        IOException,
        NoSuchAlgorithmException,
        InvalidKeyException,
        InvalidResponseException,
        XmlParserException,
        InternalException {
        return imageService.getImage(imageId);
    }

    // 이미지 수정 API
    @PatchMapping("/images/{id}")
    public void patchImg(@PathVariable("id") Long imageId, String tag, String memo) {
        imageService.patchImg(imageId, tag, memo);
    }

    // 이미지 삭제 API
    @DeleteMapping("/images/{id}")
    public Boolean deleteImg(@PathVariable("id") Long imageId) {
        imageService.softDeleteImg(imageId);
        return Boolean.TRUE;
    }
}
