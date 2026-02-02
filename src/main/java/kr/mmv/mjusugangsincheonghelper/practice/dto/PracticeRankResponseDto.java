package kr.mmv.mjusugangsincheonghelper.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 전체 랭킹 응답 (Global Snapshot)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeRankResponseDto {

    private long updatedAt;

    // key: count_N (예: count_3)
    private Map<String, RankData> data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankData {
        private List<RankEntry> total;
        // 학과별 랭킹 (key: 학과명)
        private Map<String, List<RankEntry>> dept;
        // 학년별 랭킹 (key: 학년)
        private Map<String, List<RankEntry>> grade;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankEntry {
        private int rank;
        private String name; // 마스킹 처리된 이름
        private String dept;
        private String grade;
        private long time;
    }
}
