package kr.mmv.mjusugangsincheonghelper.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import kr.mmv.mjusugangsincheonghelper.system.dto.SystemStatusDto;
import kr.mmv.mjusugangsincheonghelper.system.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시스템 상태 API 컨트롤러
 */
@Tag(name = "System", description = "시스템 상태 API")
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    @GetMapping("/status")
    @Operation(
            summary = "시스템 상태 조회",
            description = """
                    시스템 상태를 조회합니다.
                    - running: 크롤러 동작 여부 (최근 60초 내 heartbeat 확인)
                    - lastUpdatedAt: 마지막 데이터 업데이트 시간
                    - sectionCount: DB에 저장된 강의 수
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    public ResponseEntity<SingleSuccessResponseEnvelope<SystemStatusDto>> getSystemStatus() {
        SystemStatusDto status = systemService.getSystemStatus();
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(status));
    }
}
