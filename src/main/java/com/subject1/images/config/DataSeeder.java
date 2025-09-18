package com.subject1.images.config;

import com.subject1.images.entity.Image;
import com.subject1.images.repo.ImageRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.IntStream;

@Component
public class DataSeeder implements CommandLineRunner {
    private final ImageRepository imageRepository;

    public DataSeeder(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Value("${minio.url}")
    private String minio;

    @Override
    public void run(String... args) throws Exception {
        // 데이터가 10개 미만인 경우에만 실행
        if (imageRepository.count() < 10) {
            seedInitialData(10000);
        }
    }

    private void seedInitialData(int count) {
        Faker faker = new Faker();
        System.out.println(count + "개의 더미 데이터 생성을 시작합니다.");

        IntStream.range(0, count).forEach(i -> {
            Image entity = new Image();

            String fakeFileName = faker.file().fileName();
            // 이미지 데이터 처리
            entity.setProjectId(11L); // projectId는 11번으로 고정 (부하 테스트 전용)
            entity.setFileName(fakeFileName);
            entity.setStoredFileName(UUID.randomUUID().toString() + "_" + fakeFileName);
            entity.setBucketFileUrl(minio + "/" + entity.getStoredFileName());
            entity.setMemo(faker.lorem().sentence());
            entity.setTag(faker.options().option("portrait", "landscape", "nature", "city", "animal")); // 여러 태그 중 무작위 선택
            entity.setThumbnailUrl(minio + "/" + UUID.randomUUID().toString() + "thumbnail_" + fakeFileName);
            entity.setThumbnailStatus(Image.ThumbnailStatus.READY);
            entity.setHashValue(UUID.randomUUID().toString());

            // DB 저장
            imageRepository.save(entity);

            if ((i + 1) % 1000 == 0) {
                System.out.println((i + 1) + "번째 데이터 저장 완료...");
            }
        });

        System.out.println(count + "개의 더미 데이터 생성이 완료되었습니다.");
    }

}
