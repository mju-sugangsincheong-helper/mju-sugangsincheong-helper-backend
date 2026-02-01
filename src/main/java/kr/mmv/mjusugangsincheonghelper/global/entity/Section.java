package kr.mmv.mjusugangsincheonghelper.global.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 강의(분반) 엔티티
 * 크롤러가 수집한 수강신청 강의 정보
 * 
 * PK: sectioncls (명지대 공식 유일 키)
 * ERD 기준 SECTIONS 테이블
 */
@Entity
@Table(name = "sections", indexes = {
        @Index(name = "idx_section_deptcd", columnList = "deptcd"),
        @Index(name = "idx_section_curinum", columnList = "curinum"),
        @Index(name = "idx_section_curinm", columnList = "curinm"),
        @Index(name = "idx_section_active", columnList = "is_active"),
        @Index(name = "idx_section_full", columnList = "is_full"),
        @Index(name = "idx_section_curiyear_campusdiv", columnList = "curiyear, campusdiv")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section {

    /**
     * 수강신청 분반번호 (PK)
     * JSON[coursecls]: 분반 고유 식별자
     */
    @Id
    @Column(name = "sectioncls", length = 20)
    private String sectioncls;

    // ===== 기본 정보 =====
    
    /**
     * 년도
     * JSON[curiyear]: 개설년도 (예: "2025")
     */
    @Column(name = "curiyear", length = 10)
    private String curiyear;

    /**
     * 학수번호
     * JSON[curinum]: 과목코드 (예: "KMA00101")
     */
    @Column(name = "curinum", length = 20)
    private String curinum;

    /**
     * 강의명
     * JSON[curinm]: 과목명
     */
    @Column(name = "curinm", length = 200)
    private String curinm;

    /**
     * 과목 식별자
     * JSON[curinum2]: 과목 분류 코드 (예: "교필127")
     */
    @Column(name = "curinum2", length = 30)
    private String curinum2;

    /**
     * 과목그룹코드
     * JSON[groupcd]: 과목 그룹 (예: "교필", "전필" 등)
     */
    @Column(name = "groupcd", length = 20)
    private String groupcd;

    /**
     * 시간표
     * JSON[lecttime]: 강의시간 (예: "월09:00~10:50(Y2508)")
     */
    @Column(name = "lecttime", length = 500)
    private String lecttime;

    /**
     * 학점
     * JSON[cdtnum]: 학점 수
     */
    @Column(name = "cdtnum")
    private Integer cdtnum;

    /**
     * 주당 강의 시간수
     * JSON[cdttime]: 시간 수
     */
    @Column(name = "cdttime")
    private Integer cdttime;

    /**
     * 강의기간
     * JSON[lecperiod]: 강의 기간 (예: "2025-09-01 ~ 2025-12-12")
     */
    @Column(name = "lecperiod", length = 100)
    private String lecperiod;

    // ===== 교수/학과 정보 =====

    /**
     * 학과명
     * JSON[deptnm]: 개설학과명
     */
    @Column(name = "deptnm", length = 100)
    private String deptnm;

    /**
     * 학과코드
     * JSON[deptcd]: 학과 코드 (예: "10000")
     */
    @Column(name = "deptcd", length = 20)
    private String deptcd;

    /**
     * 교수명
     * JSON[profnm]: 담당 교수 이름
     */
    @Column(name = "profnm", length = 100)
    private String profnm;

    /**
     * 교수ID
     * JSON[profid]: 교수 식별자
     */
    @Column(name = "profid", length = 30)
    private String profid;

    /**
     * 캠퍼스구분
     * JSON[campusdiv]: 캠퍼스 코드 (10: 자연, 20: 인문)
     */
    @Column(name = "campusdiv", length = 10)
    private String campusdiv;

    // ===== 분류/통계 정보 =====

    /**
     * 수업유형
     * JSON[classtype]: 1=대면, 2=비대면, 3=블렌디드
     */
    @Column(name = "classtype", length = 10)
    private String classtype;

    /**
     * 대상학년
     * JSON[comyear]: 대상 학년 (0=전체, 1~4=해당학년)
     */
    @Column(name = "comyear")
    private Integer comyear;

    /**
     * 학교 장바구니 수
     * JSON[bagcnt]: 학교 시스템 장바구니 등록 수
     */
    @Column(name = "bagcnt")
    private Integer bagcnt;

    // ===== 상태 정보 =====

    /**
     * 정원
     * JSON[takelim]: 수강 제한 인원
     */
    @Column(name = "takelim")
    private Integer takelim;

    /**
     * 현재 신청인원
     * JSON[listennow]: 현재 수강 신청 인원
     */
    @Column(name = "listennow")
    private Integer listennow;

    /**
     * 만석 여부
     * Generated: (listennow >= takelim)
     */
    @Column(name = "is_full")
    @Builder.Default
    private Boolean isFull = false;

    /**
     * 활성화 여부 (폐강 시 false) (폐강 여부는 crawler 에서 없으면 자동으로 false 가 된다)
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 비활성화 시간 (폐강 시점)
     */
    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    /**
     * 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== 편의 메서드 =====

    /**
     * 만석 여부 업데이트
     */
    public void updateFullStatus() {
        this.isFull = (this.takelim != null && this.listennow != null) 
                && this.listennow >= this.takelim;
    }

    /**
     * 비활성화 (폐강 처리)
     */
    public void deactivate() {
        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
    }

    /**
     * 여석 존재 여부
     */
    public boolean hasVacancy() {
        return !Boolean.TRUE.equals(this.isFull) && Boolean.TRUE.equals(this.isActive);
    }

    /**
     * 여석 수 계산
     */
    public int getAvailableSeats() {
        if (takelim == null || listennow == null) return 0;
        return Math.max(0, takelim - listennow);
    }

    /**
     * 고유 키 (sectioncls 그 자체가 PK)
     */
    public String getUniqueKey() {
        return sectioncls;
    }
}
