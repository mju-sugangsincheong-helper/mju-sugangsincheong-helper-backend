package kr.mmv.mjusugangsincheonghelper.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import kr.mmv.mjusugangsincheonghelper.global.entity.StudentDevice;
import kr.mmv.mjusugangsincheonghelper.global.entity.Subscription;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentDeviceRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.SubscriptionRepository;
import kr.mmv.mjusugangsincheonghelper.notification.dto.NotificationDispatchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 알림 발송 서비스 (Producer)
 * 실제 발송은 하지 않고, 데이터를 조합하여 Redis Queue(mju:notification:dispatch)에 적재합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final StudentDeviceRepository studentDeviceRepository;

    private static final String DISPATCH_QUEUE = "mju:notification:dispatch";
    private static final int BATCH_SIZE = 450;

    /**
     * 여석 알림 발송 요청 처리
     * Subscription 및 Device 정보를 조회하여 Dispatch DTO 생성 후 Redis 전송
     */
    @Transactional(readOnly = true)
    public void sendVacancyNotification(List<Section> sections) {
        if (sections.isEmpty()) return;

        List<String> sectionIds = sections.stream()
                .map(Section::getSectioncls)
                .collect(Collectors.toList());

        List<Subscription> subscriptions = subscriptionRepository.findSubscribersForSectionIds(sectionIds);

        if (subscriptions.isEmpty()) {
            log.info("No subscribers for vacancy notifications");
            return;
        }

        // 강의별 그룹화
        Map<Section, List<Subscription>> groupedBySections = subscriptions.stream()
                .collect(Collectors.groupingBy(Subscription::getSection));

        for (Map.Entry<Section, List<Subscription>> entry : groupedBySections.entrySet()) {
            Section section = entry.getKey();
            List<Subscription> sectionSubscriptions = entry.getValue();

            processSectionNotification(section, sectionSubscriptions);
        }
    }

    private void processSectionNotification(Section section, List<Subscription> subscriptions) {
        // 1. 구독자들의 학생 ID 수집
        List<String> studentIds = subscriptions.stream()
                .map(s -> s.getUser().getStudentId())
                .distinct()
                .collect(Collectors.toList());

        if (studentIds.isEmpty()) return;

        // 2. 학생 ID로 모든 활성 기기 정보 조회 (JOIN FETCH)
        List<StudentDevice> devices = studentDeviceRepository.findAllActiveByStudentIdIn(studentIds);

        if (devices.isEmpty()) {
            log.info("No devices registered for section: {}", section.getCurinm());
            return;
        }

        // 3. 학생 ID -> 이름 매핑
        Map<String, String> studentNames = devices.stream()
                .collect(Collectors.toMap(
                        d -> d.getStudent().getStudentId(),
                        d -> d.getStudent().getName(),
                        (existing, replacement) -> existing
                ));

        // 4. 수신자 리스트 구성
        List<NotificationDispatchDto.Recipient> recipients = devices.stream()
                .map(device -> NotificationDispatchDto.Recipient.builder()
                        .token(device.getFcmToken())
                        .user_name(studentNames.get(device.getStudent().getStudentId()))
                        .os_family(device.getOsFamily())
                        .build())
                .collect(Collectors.toList());

        // 5. 공통 데이터 구성
        Map<String, Object> commonData = new HashMap<>();
        commonData.put("subject_name", section.getCurinm());
        commonData.put("section_code", section.getSectioncls());
        commonData.put("prof_name", section.getProfnm());
        commonData.put("vacant_seats", section.getAvailableSeats());
        commonData.put("link", "https://mju-helper.mmv.kr/subscribe");

        // 6. 배치 분할 및 전송
        dispatchInBatches(recipients, commonData);
    }

    private void dispatchInBatches(List<NotificationDispatchDto.Recipient> recipients, Map<String, Object> commonData) {
        for (int i = 0; i < recipients.size(); i += BATCH_SIZE) {
            List<NotificationDispatchDto.Recipient> batch = recipients.subList(i, Math.min(i + BATCH_SIZE, recipients.size()));

            NotificationDispatchDto dispatchDto = NotificationDispatchDto.builder()
                    .event_type("SECTION_VACANCY")
                    .priority("HIGH")
                    .common_data(commonData)
                    .recipients(batch)
                    .build();

            dispatch(dispatchDto);
        }
    }

    /**
     * 알림 발송 요청을 Redis Queue에 넣습니다.
     * FastAPI Worker가 이를 가져가서 처리합니다.
     */
    public void dispatch(NotificationDispatchDto dispatchDto) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(dispatchDto);
            redisTemplate.opsForList().leftPush(DISPATCH_QUEUE, jsonPayload);
            
            log.info("[Notification] Dispatched {} recipients for event: {}", 
                    dispatchDto.getRecipients().size(), 
                    dispatchDto.getEvent_type());
            
        } catch (JsonProcessingException e) {
            log.error("[Notification] Failed to serialize dispatch dto", e);
            throw new RuntimeException("알림 데이터 직렬화 실패", e);
        }
    }
}