package kr.mmv.mjusugangsincheonghelper.studentdevice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegisterRequestDto {

    @NotBlank(message = "FCM 토큰은 필수입니다")
    private String fcmToken;

    @Size(max = 20, message = "플랫폼 정보는 20자 이내여야 합니다")
    private String platform;

    @Size(max = 500, message = "User Agent 정보는 500자 이내여야 합니다")
    private String userAgent;
}
