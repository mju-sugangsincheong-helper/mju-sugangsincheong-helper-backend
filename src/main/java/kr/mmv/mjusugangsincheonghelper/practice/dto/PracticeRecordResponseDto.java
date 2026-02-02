package kr.mmv.mjusugangsincheonghelper.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 내 연습 기록 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeRecordResponseDto {

    private long updatedAt;
    private String studentId;
    
    // key: count_N (예: count_3)
    private Map<String, MyRecord> records;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyRecord {
        private long bestTimeMs;
        private Integer totalRank; // 전체 등수 (없으면 null)
        private Integer deptRank;  // 학과 내 등수
        private Integer gradeRank; // 학년 내 등수
    }
}
