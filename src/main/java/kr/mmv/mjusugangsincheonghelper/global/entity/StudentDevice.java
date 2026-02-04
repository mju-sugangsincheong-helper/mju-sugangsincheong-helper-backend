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
           @Index(name = "idx_student_devices_os_env", columnList = "os_family"),
           @Index(name = "idx_student_devices_fcm_token", columnList = "fcm_token")
       })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDevice {

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

    // 1. OS 정보 (macOS, Windows, iOS, Android, iPadOS 등)
    @Column(name = "os_family", length = 50)
    private String osFamily;  // "macOS", "Windows", "iOS", "Android", "iPadOS"

    @Column(name = "os_version", length = 20)
    private String osVersion; // "14.0", "11", "17.0"

    // 2. 브라우저 정보
    @Column(name = "browser_name", length = 50)
    private String browserName;  // "Chrome", "Safari", "Firefox", "Edge"

    @Column(name = "browser_version", length = 20)
    private String browserVersion; // "120.0.0"

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
     * User Agent
     * 디버깅용 기기 정보
     */
    @Column(name = "user_agent", length = 1000)
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
        updateLastActiveAt();
    }

    /**
     * Platform.js 또는 User-Agent 파싱 결과로 업데이트
     */
    public void updateDeviceInfo(
        String osFamily,
        String osVersion,
        String browserName,
        String browserVersion,
        String userAgent
    ) {
        this.osFamily = osFamily;
        this.osVersion = osVersion;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
        this.userAgent = userAgent;
        updateLastActiveAt();
    }

    /**
     * 기기 비활성화 (Soft Delete)
     */
    public void deactivate(String reason) {
        this.isActivated = false;
        this.deactivatedReason = reason;
        this.deactivatedAt = LocalDateTime.now();
    }

    public boolean isIos() {
        return "iOS".equals(this.osFamily) || "iPadOS".equals(this.osFamily);
    }

    public boolean isAndroid() {
        return "Android".equals(this.osFamily);
    }
}
