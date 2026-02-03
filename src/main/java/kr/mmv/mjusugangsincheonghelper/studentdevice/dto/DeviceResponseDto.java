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
    private String platform;
    private String status;
    private String deactivatedReason;
    private LocalDateTime deactivatedAt;
    private String modelName;
    private String userAgent;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;

    public static DeviceResponseDto from(StudentDevice device) {
        return DeviceResponseDto.builder()
                .id(device.getId())
                .fcmToken(device.getFcmToken())
                .platform(device.getPlatform().name())
                .status(device.getStatus().name())
                .deactivatedReason(device.getDeactivatedReason())
                .deactivatedAt(device.getDeactivatedAt())
                .modelName(device.getModelName())
                .userAgent(device.getUserAgent())
                .lastActiveAt(device.getLastActiveAt())
                .createdAt(device.getCreatedAt())
                .build();
    }
}
