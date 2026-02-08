package kr.mmv.mjusugangsincheonghelper.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatisticResponseDto {
    private String sectionCls;
    private long totalCount;
    private Map<String, Long> gradeCounts;
    private Map<String, Long> deptCounts;

    public static CourseStatisticResponseDto of(String sectionCls, long totalCount, Map<String, Long> gradeCounts, Map<String, Long> deptCounts) {
        return CourseStatisticResponseDto.builder()
                .sectionCls(sectionCls)
                .totalCount(totalCount)
                .gradeCounts(gradeCounts)
                .deptCounts(deptCounts)
                .build();
    }
}
