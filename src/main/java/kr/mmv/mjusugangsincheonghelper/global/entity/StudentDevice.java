package kr.mmv.mjusugangsincheonghelper.global.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 학생 디바이스 엔티티
 * FCM 토큰 및 디바이스 정보 관리
 * 한 학생이 여러 디바이스를 가질 수 있음 (멀티 디바이스 지원)
 */
@Entity
@Table(name = "student_devices", 
       uniqueConstraints = @UniqueConstraint(columnNames = "fcm_token"),
       indexes = {
           @Index(name = "idx_student_devices_student_id", columnList = "student_id"),
           @Index(name = "idx_student_devices_fcm_token", columnList = "fcm_token")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDevice {

    /**
     * 플랫폼 타입 정의
     */
    public enum DevicePlatform {
        ANDROID, IOS, PC, ETC
    }

    /**
     * 디바이스 ID (Auto Increment)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 학생 (FK)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * FCM 토큰 (Unique)
     * Firebase Cloud Messaging 푸시 알림용
     */
    @Column(name = "fcm_token", nullable = false, length = 500, unique = true)
    private String fcmToken;

    /**
     * 플랫폼
     * ANDROID, IOS, PC, ETC
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private DevicePlatform platform;

    /**
     * 알림 활성화 여부
     */
    @Column(name = "is_activated", nullable = false)
    @Builder.Default
    private boolean isActivated = true;

    /**
     * 비활성화 사유
     * 예: "UNREGISTERED", "INVALID_ARGUMENT"
     */
    @Column(name = "deactivated_reason")
    private String deactivatedReason;

    /**
     * 비활성화 시각
     */
    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    /**
     * 기기 모델명 (UI 표시용)
     * 예: "iPhone 15 Pro", "Galaxy S24", "Windows Chrome"
     */
    @Column(name = "model_name", length = 100)
    private String modelName;

    /**
     * User Agent
     * 디버깅용 기기 정보
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 마지막 활성 시간
     */
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    /**
     * 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ===== 편의 메서드 =====

    /**
     * 활성 시간 업데이트
     */
    public void updateLastActiveAt() {
        this.lastActiveAt = LocalDateTime.now();
        if (!this.isActivated) {
            this.isActivated = true;
            this.deactivatedReason = null;
            this.deactivatedAt = null;
        }
    }

    /**
     * FCM 토큰 업데이트
     */
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.lastActiveAt = LocalDateTime.now();
        this.isActivated = true;
        this.deactivatedReason = null;
        this.deactivatedAt = null;
    }

    /**
     * 디바이스 정보 업데이트
     */
    public void updateDeviceInfo(DevicePlatform platform, String modelName, String userAgent) {
        this.platform = platform;
        this.modelName = modelName;
        this.userAgent = userAgent;
        this.lastActiveAt = LocalDateTime.now();
        this.isActivated = true;
        this.deactivatedReason = null;
        this.deactivatedAt = null;
    }

    /**
     * 기기 비활성화 (Soft Delete)
     */
    public void deactivate(String reason) {
        this.isActivated = false;
        this.deactivatedReason = reason;
        this.deactivatedAt = LocalDateTime.now();
    }
}
