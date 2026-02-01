package kr.mmv.mjusugangsincheonghelper.notification.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import kr.mmv.mjusugangsincheonghelper.notification.dto.NotificationMessageDto;
import kr.mmv.mjusugangsincheonghelper.notification.service.NotificationService;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 알림 워커
 * Redis Queue(mju:section:notification:queue)에서 알림 메시지를 꺼내 처리
 * 
 * 활성화: notification.worker.enabled=true (기본값 true)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "notification.worker.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationWorker {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    
    private static final String NOTIFICATION_QUEUE = "mju:section:notification:queue";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        executor.submit(this::processNotifications);
    }
    
    @PreDestroy
    public void stop() {
        running = false;
        executor.shutdown();
    }

    private void processNotifications() {
        log.info("Notification worker started. Listening on {}", NOTIFICATION_QUEUE);
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // BRPOP: 큐에서 메시지 꺼내기 (최대 30초 대기)
                String messageJson = redisTemplate.opsForList().rightPop(NOTIFICATION_QUEUE, Duration.ofSeconds(30));
                
                if (messageJson != null) {
                    handleMessage(messageJson);
                }
            } catch (Exception e) {
                log.error("Error in notification worker", e);
                try {
                    Thread.sleep(1000); // 에러 발생 시 잠시 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void handleMessage(String json) {
        try {
            NotificationMessageDto message = objectMapper.readValue(json, NotificationMessageDto.class);
            notificationService.sendToFcm(message);
        } catch (Exception e) {
            log.error("Failed to handle notification message", e);
        }
    }
}
