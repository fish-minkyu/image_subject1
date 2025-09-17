package com.subject1.images.repo;

import com.querydsl.core.BooleanBuilder;
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

    @Override
    public List<Image> searchListCursor(Long projectId, Long lastImageId, int pageSize) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(image.softDelete.isNull()
            .or(image.softDelete.eq(Boolean.FALSE)));

        if (projectId != null) {
            builder.and(image.projectId.eq(projectId));
        }

        // Cursor 조건 추가: lastImageId보다 더 큰 이미지들
        // lastImageId가 null이면 첫 페이지이다.
        if (lastImageId != null) {
            builder.and(image.imageId.gt(lastImageId));
        }

        return queryFactory
            .selectFrom(image)
            .where(builder)
            .orderBy(image.imageId.asc())
            .limit(pageSize + 1)
            .fetch();
    }
}
