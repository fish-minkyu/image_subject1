package com.subject1.images.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {
    private static final String PACKAGE_PATH = "com.subject1";
    private static final String UTIL_ARRAYLIST = "java.util.ArrayList";
    private static final String PAGE_IMPL = "org.springframework.data.domain.PageImpl";

    @Bean
    public CacheManager redisCacheManager(
        RedisConnectionFactory redisConnectionFactory,
        ObjectMapper objectMapper
    ) {
        // LocalDateTime 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());
        // WRITE_DATES_AS_TIMESTAMPS 비활성화 -> 날짜를 배열 대신 ISO-8601 문자열로 직렬화
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Redis 직렬화&역직렬화 사용할 objectMapper 설정
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
            .builder()
            // 프로젝트 최상위 패키지 허용
            .allowIfSubType(PACKAGE_PATH)
            // List의 구현체인 ArrayList 허용
            .allowIfSubType(UTIL_ARRAYLIST)
            // Page<Image> 캐싱 시 PageImpl도 허용
            .allowIfSubType(PAGE_IMPL)
            .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer
            = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
            // 키는 String으로 직렬화
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            // 값은 JSON으로 직렬화
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer))
            // 캐시 유효 시간 5분 설정
            .entryTtl(Duration.ofMinutes(5));

        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory)
            .cacheDefaults(configuration)
            .build();
    }
}
