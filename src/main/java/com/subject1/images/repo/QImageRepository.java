package com.subject1.images.repo;

import com.subject1.images.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QImageRepository {
    // Offset 페이지네이션
    Page<Image> searchListOffset(Long projectId, Pageable pageable);

    // Cursor 페이지네이션
    List<Image> searchListCursor(Long projectId, Long lastImageId, int pageSize);
}
