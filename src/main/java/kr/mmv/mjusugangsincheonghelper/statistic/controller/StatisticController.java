package kr.mmv.mjusugangsincheonghelper.statistic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import kr.mmv.mjusugangsincheonghelper.statistic.dto.SummaryStatsResponseDto;
import kr.mmv.mjusugangsincheonghelper.statistic.dto.CourseStatisticResponseDto;
import kr.mmv.mjusugangsincheonghelper.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Statistic", description = "구독 통계 관련 API")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/subscription")
    @Operation(
            summary = "전체 구독 통계 조회",
            description = "모든 강좌의 구독 통계를 조회합니다. (19초 캐싱)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Map<String, Object>>> getSubscriptionStats() {
        Map<String, Object> stats = statisticService.getSubscriptionStats();
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(stats));
    }

    @GetMapping("/summary")
    @Operation(
            summary = "홈페이지 통계 요약 조회",
            description = """
                    공지사항 형식으로 서비스 현황을 표시합니다. (19초 캐싱)
                    
                    - 총 과목 개수: 현재 모니터링 중인 강의 수
                    - 총 구독자 수: 알림을 설정한 학생 수
                    - 정원 마감 과목: 마감된 과목 수 / 전체 과목 수
                    - TOP 10 학과: 구독자가 많은 학과 순위
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<SummaryStatsResponseDto>> getSummaryStats() {
        SummaryStatsResponseDto stats = statisticService.getSummaryStats();
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(stats));
    }

    @GetMapping("/courses/{sectionCls}")
    @Operation(
            summary = "과목별 상세 구독 통계 조회",
            description = "특정 분반의 총 구독자, 학년별/학과별 구독자 분포를 조회합니다. (30초 캐싱)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<CourseStatisticResponseDto>> getCourseStatistics(
            @PathVariable("sectionCls") String sectionCls) {
        CourseStatisticResponseDto stats = statisticService.getCourseStatistics(sectionCls);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(stats));
    }
}