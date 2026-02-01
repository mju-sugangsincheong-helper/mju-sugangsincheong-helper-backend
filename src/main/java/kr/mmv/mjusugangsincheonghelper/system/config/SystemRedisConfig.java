package kr.mmv.mjusugangsincheonghelper.system.config;

import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * System 도메인 Redis 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SystemRedisConfig {
    
    // 현재는 특별한 Redis 설정이 필요하지 않지만, 
    // sectionsync 패키지와 구조적 일관성을 위해 유지하며
    // 추후 System 관련 Redis 설정(키 관리 등)이 추가될 수 있음.
    
    public static final String REDIS_KEY_SYSTEM_STATUS = "mju:system:status";

}
