package kr.mmv.mjusugangsincheonghelper.practice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import kr.mmv.mjusugangsincheonghelper.practice.dto.SubmitPracticeRequestDto;
import kr.mmv.mjusugangsincheonghelper.practice.dto.PracticeRecordResponseDto;
import kr.mmv.mjusugangsincheonghelper.practice.dto.DepartmentRankResponseDto;
import kr.mmv.mjusugangsincheonghelper.practice.dto.PracticeRankResponseDto;
import kr.mmv.mjusugangsincheonghelper.practice.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 수강신청 연습 API
 */
@Tag(name = "Practice", description = "수강신청 연습 및 랭킹 API")
@RestController
@RequestMapping("/api/v1/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    @PostMapping
    @Operation(
            summary = "연습 결과 제출",
            description = "수강신청 연습 결과를 제출합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "제출 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Void>> submitPractice(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubmitPracticeRequestDto request) {
        practiceService.submitPractice(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SingleSuccessResponseEnvelope.empty());
    }

    @GetMapping("/ranks")
    @Operation(
            summary = "전체 랭킹 조회",
            description = "과목 수 별 전체 랭킹을 조회합니다. (30초 캐싱)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<PracticeRankResponseDto>> getGlobalRanking() {
        PracticeRankResponseDto response = practiceService.getGlobalRanking();
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));
    }

    @GetMapping("/ranks/summary")
    @Operation(
            summary = "학과별 랭킹 요약 조회",
            description = "전체 기록 중 가장 빠른 기록을 가진 상위 5개 학과를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<DepartmentRankResponseDto>> getDepartmentRankingSummary() {
        DepartmentRankResponseDto response = practiceService.getDepartmentRankingSummary();
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));
    }

    @GetMapping("/my-records")
    @Operation(
            summary = "내 기록 조회",
            description = "나의 과목 수 별 최고 기록 및 등수를 조회합니다. (개인별 캐싱)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<PracticeRecordResponseDto>> getMyPracticeRecords(
            @AuthenticationPrincipal UserDetails userDetails) {
        PracticeRecordResponseDto response = practiceService.getMyPracticeRecords(userDetails.getUsername());
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));
    }
}
