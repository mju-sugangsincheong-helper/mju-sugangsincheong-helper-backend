package kr.mmv.mjusugangsincheonghelper.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 홈페이지 통계 요약 응답 DTO
 * 공지사항 형식으로 서비스 현황을 표시
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryStatsResponseDto {

    /**
     * 총 과목 개수 (활성화된 강의)
     * 예: "현재 1,247개 과목을 실시간으로 모니터링 중이에요"
     */
    private long totalSections;

    /**
     * 총 구독자 수 (구독을 1개 이상 가진 고유 학생 수)
     * 예: "382명의 학우가 알림을 설정했어요"
     */
    private long totalSubscribers;

    /**
     * 정원 마감 과목 개수
     * 예: "정원 마감 과목 428개 / 2,543개"
     */
    private long fullSections;

    /**
     * 전체 과목 개수 (마감 비율 계산용)
     */
    private long totalSectionsForFullRatio;

    /**
     * 구독자가 많은 학과 TOP 10
     */
    private List<DepartmentSubscription> topDepartments;

    /**
     * 데이터 갱신 시점 (Unix Timestamp)
     */
    private long updatedAt;

    /**
     * 학과별 구독자 수 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentSubscription {
        private String departmentName;
        private long subscriberCount;
    }
}
