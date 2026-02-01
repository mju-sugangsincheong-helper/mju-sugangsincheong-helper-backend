package kr.mmv.mjusugangsincheonghelper.sectionapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 강의 응답 DTO
 * ERD 기준 SECTIONS 테이블 응답
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionResponseDto {

    // ===== 기본 식별자 =====
    private String sectioncls;      // PK: 분반번호

    // ===== 기본 정보 =====
    private String curiyear;        // 년도
    private String curinum;         // 학수번호
    private String curinm;          // 강의명
    private String curinum2;        // 과목 식별자 (교필127)
    private String groupcd;         // 과목그룹코드
    private String lecttime;        // 시간표
    private Integer cdtnum;         // 학점
    private Integer cdttime;        // 주당 강의 시간수
    private String lecperiod;       // 강의기간

    // ===== 교수/학과 정보 =====
    private String deptnm;          // 학과명
    private String deptcd;          // 학과코드
    private String profnm;          // 교수명
    private String profid;          // 교수ID
    private String campusdiv;       // 캠퍼스구분 (10: 자연, 20: 인문)

    // ===== 분류/통계 정보 =====
    private String classtype;       // 수업유형
    private Integer comyear;        // 대상학년
    private Integer bagcnt;         // 학교 장바구니 수

    // ===== 상태 정보 =====
    private Integer takelim;        // 정원
    private Integer listennow;      // 현재 신청인원
    private Boolean isFull;         // 만석 여부
    private Boolean isActive;       // 활성화 여부
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 여석 수
     */
    public Integer getAvailableSeats() {
        if (takelim == null || listennow == null) return null;
        return Math.max(0, takelim - listennow);
    }

    /**
     * 캠퍼스 명칭
     */
    public String getCampusName() {
        if ("10".equals(campusdiv)) return "자연캠퍼스";
        if ("20".equals(campusdiv)) return "인문캠퍼스";
        return campusdiv;
    }

    /**
     * 수업유형 명칭
     */
    public String getClasstypeName() {
        if ("1".equals(classtype)) return "대면";
        if ("2".equals(classtype)) return "비대면";
        if ("3".equals(classtype)) return "블렌디드";
        return classtype;
    }

    public static SectionResponseDto from(Section section) {
        return SectionResponseDto.builder()
                .sectioncls(section.getSectioncls())
                .curiyear(section.getCuriyear())
                .curinum(section.getCurinum())
                .curinm(section.getCurinm())
                .curinum2(section.getCurinum2())
                .groupcd(section.getGroupcd())
                .lecttime(section.getLecttime())
                .cdtnum(section.getCdtnum())
                .cdttime(section.getCdttime())
                .lecperiod(section.getLecperiod())
                .deptnm(section.getDeptnm())
                .deptcd(section.getDeptcd())
                .profnm(section.getProfnm())
                .profid(section.getProfid())
                .campusdiv(section.getCampusdiv())
                .classtype(section.getClasstype())
                .comyear(section.getComyear())
                .bagcnt(section.getBagcnt())
                .takelim(section.getTakelim())
                .listennow(section.getListennow())
                .isFull(section.getIsFull())
                .isActive(section.getIsActive())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}
