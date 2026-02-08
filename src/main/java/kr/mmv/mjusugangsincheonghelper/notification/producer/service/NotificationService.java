package kr.mmv.mjusugangsincheonghelper.notification.producer.service;

import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import java.util.List;

public interface NotificationService {
    /**
     * 여석 알림 생성 및 발행
     */
    void sendVacancyNotification(List<Section> sections);

    /**
     * 테스트 알림 발송 (로그인한 사용자의 모든 활성 기기 대상)
     */
    void sendTestNotification(String studentId);
}
