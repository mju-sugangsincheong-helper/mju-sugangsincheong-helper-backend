package kr.mmv.mjusugangsincheonghelper.statistic.dto;

import kr.mmv.mjusugangsincheonghelper.global.entity.SectionStat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionStatResponseDto {

    private StatInfo total;
    private StatInfo grade1;
    private StatInfo grade2;
    private StatInfo grade3;
    private StatInfo grade4;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatInfo {
        private Integer curr;
        private Integer max;
    }

    public static SectionStatResponseDto from(SectionStat entity) {
        return SectionStatResponseDto.builder()
                .total(new StatInfo(entity.getCurrSubscribers(), entity.getMaxSubscribers()))
                .grade1(new StatInfo(entity.getCurrGrade1Subscribers(), entity.getMaxGrade1Subscribers()))
                .grade2(new StatInfo(entity.getCurrGrade2Subscribers(), entity.getMaxGrade2Subscribers()))
                .grade3(new StatInfo(entity.getCurrGrade3Subscribers(), entity.getMaxGrade3Subscribers()))
                .grade4(new StatInfo(entity.getCurrGrade4Subscribers(), entity.getMaxGrade4Subscribers()))
                .build();
    }
}