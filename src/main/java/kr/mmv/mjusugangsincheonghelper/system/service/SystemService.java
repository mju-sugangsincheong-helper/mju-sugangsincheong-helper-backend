package kr.mmv.mjusugangsincheonghelper.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import kr.mmv.mjusugangsincheonghelper.global.repository.SectionRepository;
import kr.mmv.mjusugangsincheonghelper.system.dto.SystemStatusDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 시스템 상태 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemService {

    private final StringRedisTemplate redisTemplate;
    private final SectionRepository sectionRepository;

    private static final String REDIS_KEY_STATUS = "mju:system:status";
    private static final long HEARTBEAT_TIMEOUT_SECONDS = 60;

    /**
     * 시스템 상태 조회
     * 크롤러의 heartbeat (mju:system:status) 확인
     */
    public SystemStatusDto getSystemStatus() {
        String timestampStr = redisTemplate.opsForValue().get(REDIS_KEY_STATUS);
        long sectionCount = sectionRepository.count();
        
        boolean running;
        LocalDateTime lastUpdatedAt = null;
        String message;

        if (timestampStr != null && !timestampStr.isBlank()) {
            try {
                long timestamp = Long.parseLong(timestampStr);
                lastUpdatedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                
                // 마지막 heartbeat가 60초 이내인지 확인
                long now = System.currentTimeMillis();
                running = (now - timestamp) < (HEARTBEAT_TIMEOUT_SECONDS * 1000);
                
                if (running) {
                    message = "크롤러가 정상 동작 중입니다.";
                } else {
                    message = "크롤러 연결이 지연되고 있습니다.";
                }
            } catch (NumberFormatException e) {
                running = false;
                message = "시스템 상태를 확인할 수 없습니다.";
            }
        } else {
            running = false;
            message = sectionCount > 0 
                    ? "크롤러가 중지되어 있습니다. (기존 데이터 표시 중)"
                    : "크롤러가 아직 시작되지 않았습니다.";
        }

        return SystemStatusDto.builder()
                .running(running)
                .lastUpdatedAt(lastUpdatedAt)
                .sectionCount(sectionCount)
                .message(message)
                .build();
    }
}
