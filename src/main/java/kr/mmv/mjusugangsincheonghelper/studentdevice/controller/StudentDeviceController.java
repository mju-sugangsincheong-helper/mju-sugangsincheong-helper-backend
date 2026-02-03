package kr.mmv.mjusugangsincheonghelper.studentdevice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import kr.mmv.mjusugangsincheonghelper.studentdevice.dto.DeviceRegisterRequestDto;
import kr.mmv.mjusugangsincheonghelper.studentdevice.dto.DeviceResponseDto;
import kr.mmv.mjusugangsincheonghelper.studentdevice.service.StudentDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Device", description = "학생 기기(FCM 토큰) 관리 API")
@RestController
@RequestMapping("/api/v1/student-devices")
@RequiredArgsConstructor
public class StudentDeviceController {

    private final StudentDeviceService studentDeviceService;

    @PostMapping
    @Operation(
            summary = "디바이스 등록/갱신",
            description = "FCM 토큰을 등록하거나 기존 토큰 정보를 갱신합니다. (앱 실행 시 또는 알림 켜기 시 호출)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "등록/갱신 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.AUTH_USER_NOT_FOUND,
            ErrorCode.DEVICE_PLATFORM_INVALID,
            ErrorCode.DEVICE_LIMIT_EXCEEDED,
            ErrorCode.GLOBAL_VALIDATION_ERROR,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Void>> registerDevice(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeviceRegisterRequestDto request) {
        studentDeviceService.registerDevice(userDetails.getUsername(), request);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.empty());
    }

    @GetMapping
    @Operation(
            summary = "내 디바이스 목록 조회",
            description = "현재 사용자의 등록된 모든 디바이스 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<List<DeviceResponseDto>>> getMyDevices(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DeviceResponseDto> devices = studentDeviceService.getMyDevices(userDetails.getUsername());
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(devices));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(
            summary = "디바이스 삭제 (ID)",
            description = "특정 기기를 삭제합니다. (기기 목록에서 삭제 시 사용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.DEVICE_NOT_FOUND,
            ErrorCode.AUTH_SECURITY_FORBIDDEN_ACCESS,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Void>> deleteDeviceById(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "디바이스 ID") @PathVariable Long deviceId) {
        studentDeviceService.deleteDeviceById(userDetails.getUsername(), deviceId);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.empty());
    }

    @DeleteMapping("/token")
    @Operation(
            summary = "디바이스 삭제 (Token)",
            description = "특정 FCM 토큰을 삭제합니다. (현재 기기에서 알림 끄기 시 사용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
            ErrorCode.DEVICE_NOT_FOUND,
            ErrorCode.AUTH_SECURITY_FORBIDDEN_ACCESS,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Void>> deleteDeviceByToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "FCM 토큰") @RequestParam String fcmToken) {
        studentDeviceService.deleteDeviceByToken(userDetails.getUsername(), fcmToken);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.empty());
    }
}
