package kr.mmv.mjusugangsincheonghelper.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 알림 발송 요청 DTO (Spring -> FastAPI)
 * Redis Key: mju:notification:dispatch
 */
@Getter
@Builder
public class NotificationDispatchDto {

    /**
     * 알림 종류
     * 예: SECTION_VACANCY, NOTICE_NEW
     */
    private String event_type;

    /**
     * 중요도
     * HIGH, NORMAL
     */
    private String priority;

    /**
     * 공통 데이터
     * 템플릿의 변수로 사용됨 (예: subject_name, link)
     */
    private Map<String, Object> common_data;

    /**
     * 수신자 목록
     * 개별 사용자 정보 (토큰, 이름, 플랫폼)
     */
    private List<Recipient> recipients;

    @Getter
    @Builder
    public static class Recipient {
        private String token;
        private String user_name;
        
        // Detailed Info
        private String os_family;
    }
}
