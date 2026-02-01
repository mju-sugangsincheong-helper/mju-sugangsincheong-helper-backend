package kr.mmv.mjusugangsincheonghelper.global.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 구독 엔티티
 * 사용자가 특정 강의의 여석 알림을 구독
 */
@Entity
@Table(name = "subscriptions", 
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "section_id"}),
        indexes = {
                @Index(name = "idx_subscription_student", columnList = "student_id"),
                @Index(name = "idx_subscription_section", columnList = "section_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 구독한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student user;

    /**
     * 구독 대상 강의
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    /**
     * 알림 활성화 여부
     */
    @Builder.Default
    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = true;

    /**
     * 구독 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
