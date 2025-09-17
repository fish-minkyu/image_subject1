package com.subject1.images.dto;

import com.subject1.images.entity.Image;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImageCursorPageDto {
    private List<Image> images;
    private Long nextCursorId;
    private boolean hasNext;

    public ImageCursorPageDto(List<Image> images, Long nextCursorId, boolean hasNext) {
        this.images = images;
        this.nextCursorId = nextCursorId;
        this.hasNext = hasNext;
    }
}
