package kr.mmv.mjusugangsincheonghelper.global.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자(학생) 엔티티
 * 학번을 PK로 사용하며, 명지대 인증 API를 통해 정보를 가져옴
 */
@Entity
@Table(name = "students")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    /**
     * 학번 (Primary Key)
     * 예: 60191234
     */
    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;

    /**
     * 이름 (한글)
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 학년
     * 예: 3
     */
    @Column(length = 10)
    private String grade;

    /**
     * 이메일
     */
    @Column(length = 150)
    private String email;

    /**
     * 학과
     * 예: 컴퓨터공학과
     */
    @Column(length = 100)
    private String department;

    /**
     * 재학 상태
     * 예: 재학, 휴학, 졸업
     */
    @Column(name = "enrollment_status", length = 50)
    private String enrollmentStatus;

    /**
     * Refresh Token (JWT)
     * 로그아웃 시 삭제
     */
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    /**
     * 사용자 권한
     * ROLE_USER (기본값), ROLE_ADMIN
     */
    @Column(name = "role", length = 20)
    @Builder.Default
    private String role = "ROLE_USER";

    /**
     * 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 마지막 로그인 시간
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 활성 상태
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 비활성화 시간
     */
    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    /**
     * 학생 디바이스 목록 (FCM 토큰 관리용)
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudentDevice> devices = new ArrayList<>();

    // ===== 편의 메서드 =====

    /**
     * 토큰 정보 업데이트
     */
    public void updateTokenInfo(String refreshToken) {
        this.refreshToken = refreshToken;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 토큰 초기화 (로그아웃)
     */
    public void clearToken() {
        this.refreshToken = null;
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean isTokenValid() {
        return this.refreshToken != null;
    }

    /**
     * 계정 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.isActive = true;
        this.deactivatedAt = null;
    }

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.role);
    }
}
