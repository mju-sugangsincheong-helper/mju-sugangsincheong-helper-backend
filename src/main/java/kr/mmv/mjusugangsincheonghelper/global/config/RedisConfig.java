package kr.mmv.mjusugangsincheonghelper.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 기본 설정
 * - StringRedisTemplate: 문자열 기반 작업
 * - RedisTemplate: 객체 직렬화 작업
 * 
 * 각 도메인별 Pub/Sub 설정은 해당 도메인 패키지에서 관리
 */
@Configuration
@org.springframework.cache.annotation.EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

    @org.springframework.beans.factory.annotation.Value("${app.cache.default-ttl-seconds:60}")
    private long defaultTtl;

    @org.springframework.beans.factory.annotation.Value("${app.cache.stats-ttl-seconds:19}")
    private long statsTtl;

    @org.springframework.beans.factory.annotation.Value("${app.cache.ranking-ttl-seconds:30}")
    private long rankingTtl;

    @org.springframework.beans.factory.annotation.Value("${app.cache.departments-ttl-seconds:300}")
    private long departmentsTtl;

    /**
     * CacheManager 설정
     * - ranking: 주입된 값 (기본 30초)
     * - stats: 주입된 값 (기본 19초)
     * - default: 주입된 값 (기본 60초)
     */
    @Bean
    public org.springframework.cache.CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 1. 기본 설정 (Default)
        org.springframework.data.redis.cache.RedisCacheConfiguration defaultConfig = 
                org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(java.time.Duration.ofSeconds(defaultTtl))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper())));

        // 2. 캐시 이름별 커스텀 TTL 설정
        java.util.Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> ttlConfigs = new java.util.HashMap<>();
        
        // [A] 구독 통계 ("stats")
        ttlConfigs.put("stats", defaultConfig.entryTtl(java.time.Duration.ofSeconds(statsTtl)));

        // [B] 수강신청 연습 랭킹 ("ranking")
        ttlConfigs.put("ranking", defaultConfig.entryTtl(java.time.Duration.ofSeconds(rankingTtl)));

        // [C] 학과 목록 ("departments")
        ttlConfigs.put("departments", defaultConfig.entryTtl(java.time.Duration.ofSeconds(departmentsTtl)));

        return org.springframework.data.redis.cache.RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(ttlConfigs)
                .build();
    }
    
    /**
     * ObjectMapper 설정 (Date 직렬화 문제 해결 + 타입 정보 포함)
     */
    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 캐싱 시 타입 정보를 포함하도록 설정 (LinkedHashMap 캐스팅 오류 방지)
        objectMapper.activateDefaultTyping(
                com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return objectMapper;
    }

    /**
     * 문자열 기반 Redis Template
     * 크롤러 데이터(JSON 문자열) 처리용
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * 객체 직렬화 Redis Template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key는 문자열
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value는 JSON
        GenericJackson2JsonRedisSerializer jsonSerializer = 
                new GenericJackson2JsonRedisSerializer(objectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
