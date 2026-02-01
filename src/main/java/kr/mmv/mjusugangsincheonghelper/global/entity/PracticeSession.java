package kr.mmv.mjusugangsincheonghelper.global.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 수강신청 연습 세션 엔티티
 * 사용자의 수강신청 연습 기록
 */
@Entity
@Table(name = "practice_sessions", indexes = {
        @Index(name = "idx_practice_student", columnList = "student_id"),
        @Index(name = "idx_practice_count_time", columnList = "count_num, time_ms")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연습한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student user;

    /**
     * 장바구니 과목 수
     */
    @Column(name = "count_num", nullable = false)
    private Integer countNum;

    /**
     * 소요 시간 (밀리초)
     */
    @Column(name = "time_ms", nullable = false)
    private Long timeMs;

    /**
     * 연습 일시
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
