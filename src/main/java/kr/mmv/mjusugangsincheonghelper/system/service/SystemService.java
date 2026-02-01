package kr.mmv.mjusugangsincheonghelper.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import kr.mmv.mjusugangsincheonghelper.global.repository.SectionRepository;
import kr.mmv.mjusugangsincheonghelper.system.config.SystemRedisConfig;
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

    private static final long HEARTBEAT_TIMEOUT_SECONDS = 60;

    /**
     * 시스템 상태 조회
     * 크롤러의 heartbeat (mju:system:status) 확인
     */
    public SystemStatusDto getSystemStatus() {
        String timestampStr = redisTemplate.opsForValue().get(SystemRedisConfig.REDIS_KEY_SYSTEM_STATUS);
        long sectionCount = sectionRepository.count();
        
        boolean running;
        LocalDateTime lastUpdatedAt = null;
        String message;

        if (timestampStr != null && !timestampStr.isBlank()) {
            try {
                // Redis에 저장된 timestamp는 초 단위 (UNIX timestamp in seconds)
                long timestampSeconds = Long.parseLong(timestampStr);
                
                lastUpdatedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestampSeconds), ZoneId.systemDefault());
                
                // SETEX 60으로 설정되므로, 키가 존재한다는 것은 
                // 최근 60초 이내에 크롤러가 생존신고를 했다는 의미임.
                // 추가적인 시간 비교 없이 존재 여부만으로 판단 가능하나,
                // 안전을 위해 현재 시간과 비교 로직도 유지 (옵션)
                long nowSeconds = System.currentTimeMillis() / 1000;
                
                // 타임스탬프가 미래인 경우도 고려 (클럭 오차 등)하거나
                // 단순히 키가 있으면 Running으로 간주하는 것이 SETEX 모델에 적합
                running = true; 
                
                // 만약 만료시간 직전이라도 키가 있으면 유효함.
                // 다만 데이터가 너무 오래되었다면(TTL이 동작 안했을 경우 대비) 체크
                if (nowSeconds - timestampSeconds > HEARTBEAT_TIMEOUT_SECONDS + 10) {
                     // TTL이 60초인데 70초 이상 지났다면 뭔가 이상함 (Redis Eviction 지연 등)
                     running = false;
                }

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
