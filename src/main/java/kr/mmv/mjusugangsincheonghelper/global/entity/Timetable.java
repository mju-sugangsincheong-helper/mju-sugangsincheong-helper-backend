package kr.mmv.mjusugangsincheonghelper.global.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 시간표 엔티티
 * 사용자가 등록한 시간표
 */
@Entity
@Table(name = "timetables",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "section_id"}),
        indexes = {
                @Index(name = "idx_timetable_student", columnList = "student_id"),
                @Index(name = "idx_timetable_section", columnList = "section_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student user;

    /**
     * 등록된 강의
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    /**
     * 메모
     */
    @Column(length = 500)
    private String memo;

    /**
     * 등록 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
