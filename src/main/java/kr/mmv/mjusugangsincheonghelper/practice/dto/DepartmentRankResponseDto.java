package kr.mmv.mjusugangsincheonghelper.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRankResponseDto {
    private long updatedAt;
    private List<RankEntry> ranking;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankEntry {
        private int rank;
        private String dept;
        private BestRecord bestRecord;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BestRecord {
        private String name;
        private String grade;
        private long time;
    }
}
