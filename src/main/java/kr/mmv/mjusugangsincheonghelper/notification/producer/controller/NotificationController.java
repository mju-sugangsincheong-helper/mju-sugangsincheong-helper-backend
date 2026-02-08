package kr.mmv.mjusugangsincheonghelper.notification.producer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.notification.producer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "알림 관련 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "테스트 알림 발송", description = "현재 로그인한 사용자의 모든 활성 기기로 테스트 알림을 발송합니다.")
    @OperationErrorCodes({
        ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS,
        ErrorCode.DEVICE_NOT_FOUND,
        ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    @PostMapping("/test")
    public ResponseEntity<SingleSuccessResponseEnvelope<String>> sendTest(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.sendTestNotification(userDetails.getUsername());
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of("Test notification dispatched to queue"));
    }
}
