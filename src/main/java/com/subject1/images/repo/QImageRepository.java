package com.subject1.images.repo;

import com.subject1.images.entity.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QImageRepository {
    Page<Image> searchListOffset(Long projectId, Pageable pageable);
}
