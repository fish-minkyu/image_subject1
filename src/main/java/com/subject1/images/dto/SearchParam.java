package com.subject1.images.dto;

import com.subject1.images.entity.Image.ThumbnailStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class SearchParam {
    private String tag;
    private ThumbnailStatus thumbnailStatus;
    private Long lastImageId;
}
