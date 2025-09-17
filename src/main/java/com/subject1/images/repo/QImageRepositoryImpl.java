package com.subject1.images.repo;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.subject1.images.entity.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.subject1.images.entity.QImage.image;

@Slf4j
public class QImageRepositoryImpl implements QImageRepository {
    private final JPAQueryFactory queryFactory;

    public QImageRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Image> searchListOffset(Long projectId, Pageable pageable) {
        List<Image> content = queryFactory
                .selectFrom(image)
                .where(
                    image.projectId.eq(projectId)
                    , image.softDelete.isNull()
                    .or(image.softDelete.eq(Boolean.FALSE))
                )
                .orderBy(image.imageId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(image.count())
            .from(image);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

}
