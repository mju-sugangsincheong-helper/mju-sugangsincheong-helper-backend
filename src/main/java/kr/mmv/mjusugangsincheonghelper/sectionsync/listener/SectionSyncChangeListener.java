package kr.mmv.mjusugangsincheonghelper.sectionsync.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import kr.mmv.mjusugangsincheonghelper.sectionsync.service.SectionSyncService;

/**
 * Redis Pub/Sub 강의 변경 이벤트 리스너
 * 크롤러가 "mju:section:change" 채널에 메시지를 발행하면 수신
 * 
 * 활성화: redis.listener.section.enabled=true
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "redis.listener.section.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class SectionSyncChangeListener {

    private final SectionSyncService sectionSyncService;

    /**
     * 크롤러 이벤트 수신 핸들러
     * @param message 이벤트 메시지 (예: "updated")
     */
    public void handleMessage(String message) {
        log.info("Received section change event: {}", message);
        
        try {
            // 강의 동기화 로직 실행
            sectionSyncService.syncSections();
        } catch (Exception e) {
            log.error("Failed to sync sections on event: {}", message, e);
        }
    }
}
