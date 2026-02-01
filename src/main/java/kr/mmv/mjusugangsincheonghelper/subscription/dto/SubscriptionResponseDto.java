package kr.mmv.mjusugangsincheonghelper.subscription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.mmv.mjusugangsincheonghelper.global.entity.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구독 응답 DTO
 * ERD 기준 SECTIONS 필드 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponseDto {

    private Long id;
    private String sectioncls;      // 분반번호 (PK)
    private String curinm;          // 강의명
    private String profnm;          // 교수명
    private String deptnm;          // 학과명
    private String lecttime;        // 시간표
    private Integer takelim;        // 정원
    private Integer listennow;      // 현재 신청인원
    private Boolean isFull;         // 만석 여부
    private Boolean notificationEnabled;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 여석 수
     */
    public Integer getAvailableSeats() {
        if (takelim == null || listennow == null) return null;
        return Math.max(0, takelim - listennow);
    }

    public static SubscriptionResponseDto from(Subscription subscription) {
        return SubscriptionResponseDto.builder()
                .id(subscription.getId())
                .sectioncls(subscription.getSection().getSectioncls())
                .curinm(subscription.getSection().getCurinm())
                .profnm(subscription.getSection().getProfnm())
                .deptnm(subscription.getSection().getDeptnm())
                .lecttime(subscription.getSection().getLecttime())
                .takelim(subscription.getSection().getTakelim())
                .listennow(subscription.getSection().getListennow())
                .isFull(subscription.getSection().getIsFull())
                .notificationEnabled(subscription.getNotificationEnabled())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
