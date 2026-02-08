package kr.mmv.mjusugangsincheonghelper.notification.consumer.worker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import kr.mmv.mjusugangsincheonghelper.notification.common.dto.FcmMessageDto;
import kr.mmv.mjusugangsincheonghelper.notification.consumer.service.FcmSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Redis Queue(mju:notification:dispatch)를 모니터링하여 알림 발송을 위임하는 워커
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final FcmSenderService fcmSenderService;

    private static final String DISPATCH_QUEUE = "mju:notification:dispatch";
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("fcm-dispatcher-1");
        return thread;
    });
    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        executor.submit(this::processQueue);
        log.info("[NotificationWorker] Worker thread started. Listening on {}", DISPATCH_QUEUE);
    }

    @PreDestroy
    public void stop() {
        running = false;
        executor.shutdown();
        log.info("[NotificationWorker] Worker thread stopped.");
    }

    private void processQueue() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Redis BRPOP (30초 타임아웃)
                String json = redisTemplate.opsForList().rightPop(DISPATCH_QUEUE, Duration.ofSeconds(30));

                if (json != null) {
                    handleDispatch(json);
                }
            } catch (Exception e) {
                log.error("[NotificationWorker] Error in worker loop", e);
                try {
                    Thread.sleep(5000); // 에러 발생 시 대기 후 재시도
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void handleDispatch(String json) {
        try {
            // 메시지 리스트로 역직렬화
            List<FcmMessageDto> messages = objectMapper.readValue(json, new TypeReference<List<FcmMessageDto>>() {});
            
            if (messages != null && !messages.isEmpty()) {
                fcmSenderService.sendMessages(messages);
            }
        } catch (Exception e) {
            log.error("[NotificationWorker] Failed to parse notification dispatch JSON: {}", json, e);
        }
    }
}
