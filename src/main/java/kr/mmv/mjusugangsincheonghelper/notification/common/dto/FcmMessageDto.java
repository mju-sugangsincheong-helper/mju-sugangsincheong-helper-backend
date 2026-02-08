package kr.mmv.mjusugangsincheonghelper.notification.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * FCM 전송을 위한 공통 DTO
 * Redis에 JSON 리스트 형태로 저장됩니다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmMessageDto {
    private String token;
    private String topic;
    private NotificationDto notification;
    private Map<String, String> data;
    private WebpushDto webpush;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDto {
        private String title;
        private String body;
        private String image;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebpushDto {
        private Map<String, String> headers;
        private Map<String, String> data;
        private WebpushNotificationDto notification;
        
        @JsonProperty("fcm_options")
        private FcmOptionsDto fcmOptions;

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WebpushNotificationDto {
            private String title;
            private String body;
            private String icon;
            private String image;
            private String language;
            private Boolean renotify;
            private Boolean requireInteraction;
            private Long timestamp;
            private List<ActionDto> actions;
        }

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActionDto {
            private String action;
            private String title;
            private String icon;
        }

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FcmOptionsDto {
            private String link;
        }
    }
}
