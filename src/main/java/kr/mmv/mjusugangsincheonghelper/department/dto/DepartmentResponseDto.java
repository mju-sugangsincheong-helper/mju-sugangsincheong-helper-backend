package kr.mmv.mjusugangsincheonghelper.department.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 학과 정보 응답 DTO
 * Section 테이블에서 DISTINCT 조회된 학과 정보
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponseDto {
    
    /**
     * 학과 코드
     */
    private String deptcd;
    
    /**
     * 학과명
     */
    private String deptnm;
    
    /**
     * 캠퍼스 구분 코드 (10: 자연, 20: 인문)
     */
    private String campusdiv;
    
    /**
     * 캠퍼스명 (자연캠퍼스, 인문캠퍼스)
     */
    private String campusName;
    
    /**
     * Object[] 결과를 DTO로 변환
     */
    public static DepartmentResponseDto from(Object[] row) {
        String campusdiv = (String) row[2];
        return DepartmentResponseDto.builder()
                .deptcd((String) row[0])
                .deptnm((String) row[1])
                .campusdiv(campusdiv)
                .campusName(getCampusName(campusdiv))
                .build();
    }
    
    /**
     * 캠퍼스 코드 → 캠퍼스명 변환
     */
    private static String getCampusName(String campusdiv) {
        if ("10".equals(campusdiv)) {
            return "자연캠퍼스";
        } else if ("20".equals(campusdiv)) {
            return "인문캠퍼스";
        }
        return "기타";
    }
}
