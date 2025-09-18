package com.subject1.images.dto;

import com.subject1.images.entity.Image;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class SearchParam {
    private String tag;
    private Image.ThumbnailStatus thumbnailStatus;
    private Long lastImageId;
}
