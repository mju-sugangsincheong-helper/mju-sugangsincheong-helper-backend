package kr.mmv.mjusugangsincheonghelper.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import kr.mmv.mjusugangsincheonghelper.subscription.dto.SubscriptionRequestDto;
import kr.mmv.mjusugangsincheonghelper.subscription.dto.SubscriptionResponseDto;
import kr.mmv.mjusugangsincheonghelper.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 구독 API 컨트롤러
 */
@Tag(name = "Subscription", description = "여석 알림 구독 API")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(
            summary = "강의 구독",
            description = "특정 강의의 여석 알림을 구독합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "구독 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.SECTION_NOT_FOUND,
            ErrorCode.SUBSCRIPTION_ALREADY_EXISTS,
            ErrorCode.SUBSCRIPTION_LIMIT_EXCEEDED,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<SubscriptionResponseDto>> subscribe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SubscriptionRequestDto request) {
        SubscriptionResponseDto response = subscriptionService.subscribe(
                userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SingleSuccessResponseEnvelope.of(response));
    }

    @DeleteMapping("/{sectioncls}")
    @Operation(
            summary = "구독 취소",
            description = "특정 강의의 구독을 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "구독 취소 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.SECTION_NOT_FOUND,
            ErrorCode.SUBSCRIPTION_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Void>> unsubscribe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "수강신청 강의 번호 (sectioncls)") @PathVariable String sectioncls) {
        subscriptionService.unsubscribe(userDetails.getUsername(), sectioncls);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.empty());
    }

    @GetMapping
    @Operation(
            summary = "내 구독 목록 조회",
            description = "현재 사용자의 구독 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<List<SubscriptionResponseDto>>> getMySubscriptions(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SubscriptionResponseDto> subscriptions = subscriptionService.getMySubscriptions(
                userDetails.getUsername());
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(subscriptions));
    }
}
