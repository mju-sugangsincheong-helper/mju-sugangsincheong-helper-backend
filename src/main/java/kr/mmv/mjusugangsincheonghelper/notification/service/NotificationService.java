package kr.mmv.mjusugangsincheonghelper.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import kr.mmv.mjusugangsincheonghelper.notification.dto.NotificationMessageDto;

/**
 * 알림 전송 서비스
 * Worker가 수신한 메시지를 실제 FCM 서버로 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /**
     * FCM으로 알림 전송 (시뮬레이션)
     */
    public void sendToFcm(NotificationMessageDto message) {
        // 실제 FCM 연동 로직이 들어갈 곳
        // 현재는 로그로 대체
        
        log.info("==========================================");
        log.info("[FCM SEND] Section: {} ({})", message.getCurinm(), message.getSectioncls());
        log.info("Vacancy: {}", message.getVacancy());
        log.info("Targets (FCM): {}", message.getFcmTokens().size());
        log.info("Targets (Email): {}", message.getEmails().size());
        log.info("==========================================");
        
        // TODO: FCM SDK를 사용하여 메시지 전송 구현
        // FirebaseMessaging.getInstance().sendMulticast(...)
    }
}
