package com.subject1.images.controller;

import com.subject1.images.dto.ImageCursorPageDto;
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
@RequestMapping("/projects")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // 이미지 업로드 API
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

    // 이미지 목록 조회 API
    @GetMapping("/{projectId}/images")
    public ResponseEntity<?> getListImg(
        @PathVariable Long projectId,
        @RequestParam(required = false) Long lastImageId,
        @PageableDefault(size = 10) Pageable pageable) {
        // Offset 방식
//        Page<Image> offsetPage = imageService.getListImgListOffset(projectId, pageable);
//        return ResponseEntity.ok(offsetPage);

        // Cursor 방식
        int pageSize = pageable.getPageSize() > 0 ? pageable.getPageSize() : 10;
        ImageCursorPageDto cursorPage = imageService.getListImgListCursor(projectId, lastImageId, pageSize);
        return ResponseEntity.ok(cursorPage);
    }

    // 이미지 수정 API
    @PatchMapping("/images/{id}")
    public Image patchImg(@PathVariable("id") Long imageId, String tag, String memo) {
        return imageService.patchImg(imageId, tag, memo);
    }

    // 이미지 삭제 API
    @DeleteMapping("/images/{id}")
    public Boolean deleteImg(@PathVariable("id") Long imageId) {
        imageService.softDeleteImg(imageId);
        return Boolean.TRUE;
    }
}
