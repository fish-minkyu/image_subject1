package com.subject1.images.dto;

import lombok.Getter;

@Getter
public class SearchParam {
    private Long projectId;
    private String tag;
    private Long lastImageId;
    // 추후 상태 필드 추가 필요
}
