package kr.mmv.mjusugangsincheonghelper.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 알림 메시지 DTO (Redis Queue용)
 * ERD 기준 SECTIONS 테이블 필드 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDto {

    /**
     * 분반 고유 식별자 (PK)
     */
    private String sectioncls;

    /**
     * 강의명
     */
    private String curinm;

    /**
     * 교수명
     */
    private String profnm;

    /**
     * 학과코드
     */
    private String deptcd;

    /**
     * 학과명
     */
    private String deptnm;

    /**
     * 현재 여석 수
     */
    private Integer vacancy;

    /**
     * 알림 대상 FCM 토큰 목록
     */
    private List<String> fcmTokens;

    /**
     * 알림 대상 이메일 목록
     */
    private List<String> emails;
}
