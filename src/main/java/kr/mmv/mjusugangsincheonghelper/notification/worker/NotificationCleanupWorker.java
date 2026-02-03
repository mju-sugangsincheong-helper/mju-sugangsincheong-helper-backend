package kr.mmv.mjusugangsincheonghelper.notification.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 알림 후처리 워커 (Cleanup & Monitor)
 * 1. FastAPI가 반환한 '죽은 토큰'을 DB에서 삭제
 * 2. 알림 서버(FastAPI)의 생존 여부 확인 (Optional)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "notification.worker.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationCleanupWorker {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final StudentDeviceRepository studentDeviceRepository;

    private static final String CLEANUP_QUEUE = "mju:device:cleanup";
    private static final String STATUS_KEY = "mju:notification:status";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        executor.submit(this::processCleanup);
    }

    @PreDestroy
    public void stop() {
        running = false;
        executor.shutdown();
    }

    /**
     * 죽은 토큰 정리 루프
     * FastAPI가 유효하지 않은 토큰(Unregistered 등)을 발견하면 이 큐에 넣습니다.
     */
    private void processCleanup() {
        log.info("[NotificationCleanup] Worker started. Listening on {}", CLEANUP_QUEUE);
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // BRPOP: 큐에서 메시지 꺼내기 (최대 30초 대기 - Redis Timeout 60초보다 짧아야 함)
                String messageJson = redisTemplate.opsForList().rightPop(CLEANUP_QUEUE, Duration.ofSeconds(30));

                if (messageJson != null) {
                    handleCleanup(messageJson);
                }
            } catch (Exception e) {
                log.error("[NotificationCleanup] Error in worker loop", e);
                try {
                    Thread.sleep(5000); // 에러 발생 시 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    protected void handleCleanup(String json) {
        try {
            // List<String> 형태의 토큰 리스트 파싱
            List<String> invalidTokens = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            
            if (invalidTokens != null && !invalidTokens.isEmpty()) {
                log.info("[NotificationCleanup] Deactivating {} invalid tokens", invalidTokens.size());
                // 하나씩 비활성화 (Soft Delete)
                for (String token : invalidTokens) {
                    try {
                        studentDeviceRepository.findByFcmToken(token).ifPresent(device -> {
                            device.deactivate("UNREGISTERED");
                            studentDeviceRepository.save(device);
                            log.info("[NotificationCleanup] Deactivated token: {}", token.substring(0, 10) + "...");
                        });
                    } catch (Exception e) {
                        log.error("[NotificationCleanup] Failed to deactivate token: {}", token, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[NotificationCleanup] Failed to process cleanup message: {}", json, e);
        }
    }

    /**
     * 알림 서버 상태 확인 (Health Check)
     * FastAPI가 주기적으로 이 키를 갱신해야 함 (SET ... EX 60)
     */
    public boolean isNotificationServerRunning() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(STATUS_KEY));
    }
}
