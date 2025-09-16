package com.subject1.images.repo;

import com.subject1.images.entity.Image;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long>, QImageRepository {

    // 해시 값으로 이미지 조회
    Optional<Image> findByHashValue(String hashValue);

    // 해시 값으로 이미지를 조회하면서 해당 레코드에 락을 거는 쿼리
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락 설정
    @Query("SELECT i FROM Image i WHERE i.hashValue = :hashValue")
    Optional<Image> findForUpdateByHashValue(@Param("hashValue") String hashValue);
}
