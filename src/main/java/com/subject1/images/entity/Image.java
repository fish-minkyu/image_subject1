package com.subject1.images.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    // 파일명
    @Column(nullable = false)
    private String fileName;

    // MinIO에 실제 저장될 고유한 파일 이름
    @Column(unique = false, nullable = false)
    private String storedFileName;

    // 원본 이미지가 MinIO에 저장된 URL
    private String fileUrl;

    // 썸네일 이미지가 MinIO에 저장된 경로
    private String thumbnailUrl;

    // 썸네일 상태값
    @Enumerated(EnumType.STRING)
    private ThumbnailStatus thumbnailStatus;

    // 해시 값
    @Column(unique = true, nullable = false)
    private String hashValue;

    // 소프트 삭제 여부
    private Boolean isDeleted;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 썸네일 상태 enum
    public enum ThumbnailStatus {
        NONE,
        PROCESSING,
        READY,
        FAILED
    }
}
