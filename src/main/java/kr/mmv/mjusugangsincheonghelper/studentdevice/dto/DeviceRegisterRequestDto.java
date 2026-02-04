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

    private String oldToken;

    // 1. OS 정보
    @Size(max = 50, message = "OS 이름은 50자 이내여야 합니다")
    private String osFamily;

    @Size(max = 20, message = "OS 버전은 20자 이내여야 합니다")
    private String osVersion;

    // 2. 브라우저 정보
    @Size(max = 50, message = "브라우저 이름은 50자 이내여야 합니다")
    private String browserName;

    @Size(max = 20, message = "브라우저 버전은 20자 이내여야 합니다")
    private String browserVersion;

    @Size(max = 1000, message = "User Agent 정보는 1000자 이내여야 합니다")
    private String userAgent;
}
