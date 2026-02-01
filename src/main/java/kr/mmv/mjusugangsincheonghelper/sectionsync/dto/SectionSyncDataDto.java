package kr.mmv.mjusugangsincheonghelper.sectionsync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 크롤러에서 수집한 강의 데이터 DTO
 * JSON 필드명과 정확히 매핑 (명지대 API 원본 구조)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionSyncDataDto {

    // ===== 기본 식별자 =====
    
    /**
     * 분반 고유 식별자 (PK)
     * coursecls: 수강신청 분반번호
     */
    @JsonProperty("coursecls")
    private String coursecls;

    // ===== 기본 정보 =====
    
    /**
     * 년도
     */
    @JsonProperty("curiyear")
    private String curiyear;

    /**
     * 학수번호
     */
    @JsonProperty("curinum")
    private String curinum;

    /**
     * 강의명
     */
    @JsonProperty("curinm")
    private String curinm;

    /**
     * 과목 식별자 (예: "교필127")
     */
    @JsonProperty("curinum2")
    private String curinum2;

    /**
     * 과목그룹코드 (예: "교필", "전필")
     */
    @JsonProperty("groupcd")
    private String groupcd;

    /**
     * 시간표 (예: "월09:00~10:50(Y2508)")
     */
    @JsonProperty("lecttime")
    private String lecttime;

    /**
     * 학점
     */
    @JsonProperty("cdtnum")
    private String cdtnum;

    /**
     * 주당 강의 시간수
     */
    @JsonProperty("cdttime")
    private String cdttime;

    /**
     * 강의기간
     */
    @JsonProperty("lecperiod")
    private String lecperiod;

    // ===== 교수/학과 정보 =====

    /**
     * 학과명
     */
    @JsonProperty("deptnm")
    private String deptnm;

    /**
     * 학과코드
     */
    @JsonProperty("deptcd")
    private String deptcd;

    /**
     * 교수명
     */
    @JsonProperty("profnm")
    private String profnm;

    /**
     * 교수ID
     */
    @JsonProperty("profid")
    private String profid;

    /**
     * 캠퍼스구분 (10: 자연, 20: 인문)
     */
    @JsonProperty("campusdiv")
    private String campusdiv;

    // ===== 분류/통계 정보 =====

    /**
     * 수업유형 (1=대면, 2=비대면, 3=블렌디드)
     */
    @JsonProperty("classtype")
    private String classtype;

    /**
     * 대상학년 (0=전체, 1~4=해당학년)
     */
    @JsonProperty("comyear")
    private String comyear;

    /**
     * 학교 장바구니 수
     */
    @JsonProperty("bagcnt")
    private String bagcnt;

    // ===== 상태 정보 =====

    /**
     * 정원
     */
    @JsonProperty("takelim")
    private String takelim;

    /**
     * 현재 신청인원
     */
    @JsonProperty("listennow")
    private String listennow;

    // ===== 편의 메서드 =====

    /**
     * 고유 키 (coursecls = sectioncls)
     */
    public String getUniqueKey() {
        return coursecls;
    }

    /**
     * 만석 여부 확인
     */
    public boolean isFull() {
        return safeParseInt(listennow) >= safeParseInt(takelim) && safeParseInt(takelim) > 0;
    }

    /**
     * 학점 정수 변환
     */
    public Integer getCdtnumInt() {
        return safeParseInt(cdtnum);
    }

    /**
     * 주당 강의 시간 정수 변환
     */
    public Integer getCdttimeInt() {
        return safeParseInt(cdttime);
    }

    /**
     * 대상학년 정수 변환
     */
    public Integer getComyearInt() {
        return safeParseInt(comyear);
    }

    /**
     * 장바구니 수 정수 변환
     */
    public Integer getBagcntInt() {
        return safeParseInt(bagcnt);
    }

    /**
     * 정원 정수 변환
     */
    public Integer getTakelimInt() {
        return safeParseInt(takelim);
    }

    /**
     * 현재 신청인원 정수 변환
     */
    public Integer getListennowInt() {
        return safeParseInt(listennow);
    }

    /**
     * 안전한 정수 파싱
     */
    private Integer safeParseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
