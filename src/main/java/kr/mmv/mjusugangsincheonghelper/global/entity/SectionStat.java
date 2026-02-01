package kr.mmv.mjusugangsincheonghelper.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "section_stats")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SectionStat {

    @Id
    @Column(name = "sectioncls", length = 20)
    private String sectionCls;

    // --- 현재 상태 ---
    @Column(name = "takelim")
    private Integer takeLim;

    @Column(name = "listennow")
    private Integer listenNow;

    @Column(name = "available_seats")
    private Integer availableSeats;

    // --- 구독자 수 (현재) ---
    @Column(name = "curr_subscribers")
    private Integer currSubscribers;

    @Column(name = "curr_grade1_subscribers")
    private Integer currGrade1Subscribers;

    @Column(name = "curr_grade2_subscribers")
    private Integer currGrade2Subscribers;

    @Column(name = "curr_grade3_subscribers")
    private Integer currGrade3Subscribers;

    @Column(name = "curr_grade4_subscribers")
    private Integer currGrade4Subscribers;

    // --- 구독자 수 (최대) ---
    @Column(name = "max_subscribers")
    private Integer maxSubscribers;

    @Column(name = "max_grade1_subscribers")
    private Integer maxGrade1Subscribers;

    @Column(name = "max_grade2_subscribers")
    private Integer maxGrade2Subscribers;

    @Column(name = "max_grade3_subscribers")
    private Integer maxGrade3Subscribers;

    @Column(name = "max_grade4_subscribers")
    private Integer maxGrade4Subscribers;

    // --- 메타 ---
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "max_updated")
    private LocalDateTime maxUpdated;
}
