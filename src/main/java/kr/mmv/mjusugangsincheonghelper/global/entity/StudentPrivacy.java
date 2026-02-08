package kr.mmv.mjusugangsincheonghelper.global.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * 학생 개인정보 동의 및 관련 정보 저장 엔티티
 */
@Entity
@Table(name = "student_privacy")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPrivacy implements Persistable<String> {

    /**
     * 학번 (Primary Key이자 Student 엔티티의 Foreign Key)
     */
    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "student_id")
    private Student student;

    /**
     * 저장된 데이터 (이름, 학년, 학과, 이메일, 기기정보 등)
     * JSON 형태나 상세 텍스트로 저장
     */
    @Column(name = "data", columnDefinition = "TEXT")
    private String data;

    /**
     * 개인정보 처리방침 동의 여부
     */
    @Column(name = "is_agreed", nullable = false)
    private Boolean isAgreed;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public String getId() {
        return this.studentId;
    }

    @Override
    public boolean isNew() {
        return this.createdAt == null;
    }
}
