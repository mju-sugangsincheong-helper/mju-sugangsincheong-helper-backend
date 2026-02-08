package kr.mmv.mjusugangsincheonghelper.notification.producer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.BaseException;
import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import kr.mmv.mjusugangsincheonghelper.global.entity.StudentDevice;
import kr.mmv.mjusugangsincheonghelper.global.entity.Subscription;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentDeviceRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.SubscriptionRepository;
import kr.mmv.mjusugangsincheonghelper.notification.common.dto.FcmMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 알림 생성 및 발행 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final StudentDeviceRepository studentDeviceRepository;

    private static final String DISPATCH_QUEUE = "mju:notification:dispatch";

    @Override
    @Transactional(readOnly = true)
    public void sendVacancyNotification(List<Section> sections) {
        if (sections == null || sections.isEmpty()) return;

        List<String> sectionIds = sections.stream()
                .map(Section::getSectioncls)
                .collect(Collectors.toList());

        List<Subscription> subscriptions = subscriptionRepository.findSubscribersForSectionIds(sectionIds);
        if (subscriptions.isEmpty()) {
            log.info("[Producer] No subscribers for vacancy notification");
            return;
        }

        Map<Section, List<Subscription>> grouped = subscriptions.stream()
                .collect(Collectors.groupingBy(Subscription::getSection));

        List<FcmMessageDto> allMessages = new ArrayList<>();

        for (Map.Entry<Section, List<Subscription>> entry : grouped.entrySet()) {
            Section section = entry.getKey();
            List<Subscription> sectionSubs = entry.getValue();
            allMessages.addAll(createVacancyMessages(section, sectionSubs));
        }

        if (!allMessages.isEmpty()) {
            dispatch(allMessages);
        }
    }

    private List<FcmMessageDto> createVacancyMessages(Section section, List<Subscription> subscriptions) {
        List<String> studentIds = subscriptions.stream()
                .map(s -> s.getUser().getStudentId())
                .distinct()
                .collect(Collectors.toList());

        List<StudentDevice> devices = studentDeviceRepository.findAllActiveByStudentIdIn(studentIds);
        
        return devices.stream().map(device -> {
            String userName = device.getStudent().getName();
            String title = "여석 알림";
            String body = String.format("%s님 %s 강의에 여석이 %d개 생겼어요", 
                    userName, section.getCurinm(), section.getAvailableSeats());
            
            return createBasePersonalizedMessage(device.getFcmToken(), title, body, "SECTION_VACANCY", "high");
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public void sendTestNotification(String studentId) {
        List<StudentDevice> devices = studentDeviceRepository.findActiveByStudentId(studentId);
        
        if (devices.isEmpty()) {
            throw new BaseException(ErrorCode.DEVICE_NOT_FOUND);
        }

        List<FcmMessageDto> testMessages = devices.stream().map(device -> {
            String userName = device.getStudent().getName();
            String title = "테스트 알림";
            String body = String.format("%s님 알림이 잘 도착하는거 같아요!!", userName);
            
            return createBasePersonalizedMessage(device.getFcmToken(), title, body, "TEST", "normal");
        }).collect(Collectors.toList());
        
        dispatch(testMessages);
    }

    /**
     * 공통적인 알림 DTO 생성 로직 (규격화된 페이로드)
     */
    private FcmMessageDto createBasePersonalizedMessage(String token, String title, String body, String type, String urgency) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("urgency", urgency);
        data.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        return FcmMessageDto.builder()
                .token(token)
                .notification(FcmMessageDto.NotificationDto.builder()
                        .title(title)
                        .body(body)
                        .build())
                .data(data)
                .build();
    }

    /**
     * 알림 메시지 리스트를 JSON으로 변환하여 Redis Queue에 적재
     */
    private void dispatch(List<FcmMessageDto> messages) {
        if (messages == null || messages.isEmpty()) return;
        
        try {
            String json = objectMapper.writeValueAsString(messages);
            redisTemplate.opsForList().leftPush(DISPATCH_QUEUE, json);
            log.info("[Producer] Dispatched {} messages to queue: {}", messages.size(), DISPATCH_QUEUE);
        } catch (JsonProcessingException e) {
            log.error("[Producer] Serialization failed for notification messages", e);
        }
    }
}