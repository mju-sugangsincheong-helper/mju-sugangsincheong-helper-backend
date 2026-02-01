package kr.mmv.mjusugangsincheonghelper.global.api.meta;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMeta {
    private String requestId;
    private String apiVersion;
    private String clientIp;
    private String userAgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    private Long durationMs;
}
