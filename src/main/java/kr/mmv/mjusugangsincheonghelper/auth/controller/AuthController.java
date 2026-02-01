package kr.mmv.mjusugangsincheonghelper.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.mmv.mjusugangsincheonghelper.auth.dto.*;
import kr.mmv.mjusugangsincheonghelper.auth.service.AuthService;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 * 명지대 학생 인증 기반 로그인/회원가입 통합
 */
@Tag(name = "Auth", description = "인증 API (명지대 학생 인증)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "로그인 (회원가입 자동 처리)",
            description = """
                    명지대 학번과 비밀번호로 인증합니다.
                    - 처음 로그인하는 경우 자동으로 회원가입이 진행됩니다.
                    - 학부생(학번이 60으로 시작)만 이용 가능합니다.
                    - 인증 성공 시 JWT 토큰을 발급합니다. (14일 유효)
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.MJU_UNIV_AUTH_INVALID_CREDENTIALS,
            ErrorCode.MJU_UNIV_AUTH_NOT_UNDERGRADUATE,
            ErrorCode.MJU_UNIV_AUTH_INVALID_STUDENT_ID,
            ErrorCode.MJU_UNIV_AUTH_NETWORK_ERROR,
            ErrorCode.GLOBAL_VALIDATION_ERROR,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<TokenResponseDto>> login(
            @Valid @RequestBody AuthRequestDto request) {
        TokenResponseDto response = authService.authenticate(request);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 갱신",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_INVALID_TOKEN,
            ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND,
            ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED,
            ErrorCode.AUTH_USER_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<TokenResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        TokenResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "현재 사용자의 리프레시 토큰을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.AUTH_USER_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.empty());
    }

    @PostMapping("/withdraw")
    @Operation(
            summary = "회원 탈퇴",
            description = """
                    회원 탈퇴를 수행합니다.
                    - 사용자의 계정을 비활성화합니다. (is_active = false)
                    - 사용자의 구독 목록 및 디바이스 정보를 모두 삭제합니다.
                    - Refresh Token을 삭제합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "탈퇴 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.AUTH_USER_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Void>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.withdraw(userDetails.getUsername());
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.empty());
    }

    @GetMapping("/me")
    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "인증된 사용자의 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.AUTH_USER_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<UserResponseDto>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDto response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));
    }
}
