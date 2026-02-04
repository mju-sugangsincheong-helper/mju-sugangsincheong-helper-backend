package kr.mmv.mjusugangsincheonghelper.studentdevice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import kr.mmv.mjusugangsincheonghelper.global.entity.StudentDevice;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponseDto {

    private Long id;
    private String fcmToken;
    
    private String osFamily;
    private String osVersion;
    private String browserName;
    private String browserVersion;
    
    private boolean isActivated;
    private String deactivatedReason;
    private LocalDateTime deactivatedAt;
    private String userAgent;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;

    public static DeviceResponseDto from(StudentDevice device) {
        return DeviceResponseDto.builder()
                .id(device.getId())
                .fcmToken(device.getFcmToken())
                .osFamily(device.getOsFamily())
                .osVersion(device.getOsVersion())
                .browserName(device.getBrowserName())
                .browserVersion(device.getBrowserVersion())
                .isActivated(device.isActivated())
                .deactivatedReason(device.getDeactivatedReason())
                .deactivatedAt(device.getDeactivatedAt())
                .userAgent(device.getUserAgent())
                .lastActiveAt(device.getLastActiveAt())
                .createdAt(device.getCreatedAt())
                .build();
    }
}
