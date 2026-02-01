package kr.mmv.mjusugangsincheonghelper.statistic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import kr.mmv.mjusugangsincheonghelper.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}