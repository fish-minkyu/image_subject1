package com.subject1.images.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class SearchParam {
    private String tag;
    private Long lastImageId;
    // 추후 상태 필드 추가 필요
}
