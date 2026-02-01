package kr.mmv.mjusugangsincheonghelper.global.api.support;

import java.time.LocalDateTime;

import kr.mmv.mjusugangsincheonghelper.global.api.meta.ClientInfo;
import kr.mmv.mjusugangsincheonghelper.global.api.meta.ResponseMeta;

/**
 * Envelope 생성 시 자동 주입
 */
public class MetaGenerator {

    public static ResponseMeta auto() {
        String requestId = CustomResponseMetaContextHolder.getRequestId();
        String apiVersion = CustomResponseMetaContextHolder.getApiVersion();
        Long startTime = CustomResponseMetaContextHolder.getStartTime();
        Long duration = (startTime != null) ? System.currentTimeMillis() - startTime : null;

        ClientInfo client = CustomResponseMetaContextHolder.getClientInfo();

        return ResponseMeta.builder()
                .requestId(requestId != null ? requestId : "unknown")
                .apiVersion(apiVersion != null ? apiVersion : "v1")
                .clientIp(client != null ? client.getIp() : "unknown")
                .userAgent(client != null ? client.getUserAgent() : "unknown")
                .timestamp(LocalDateTime.now())
                .durationMs(duration)
                .build();
    }
}
