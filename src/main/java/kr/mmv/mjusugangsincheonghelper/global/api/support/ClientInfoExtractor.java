package kr.mmv.mjusugangsincheonghelper.global.api.support;

import jakarta.servlet.http.HttpServletRequest;
import kr.mmv.mjusugangsincheonghelper.global.api.meta.ClientInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class ClientInfoExtractor {

    public ClientInfo extract(HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String host = request.getHeader("Host");
        String forwardedFor = request.getHeader("X-Forwarded-For");

        return ClientInfo.builder()
                .ip(ip)
                .userAgent(userAgent != null ? userAgent : "unknown")
                .host(host)
                .forwardedFor(forwardedFor)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR",
                "X-Real-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 헤더는 여러 IP가 콤마로 구분될 수 있음
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}
