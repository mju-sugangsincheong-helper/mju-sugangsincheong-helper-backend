package kr.mmv.mjusugangsincheonghelper.global.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.mmv.mjusugangsincheonghelper.global.api.support.ClientInfoExtractor;
import kr.mmv.mjusugangsincheonghelper.global.api.support.CustomResponseMetaContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 요청 진입 시 메타데이터 설정 + 종료 시 정리
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalMetaFilter extends OncePerRequestFilter {

    private final ClientInfoExtractor clientInfoExtractor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // [1] 메타데이터 설정
            CustomResponseMetaContextHolder.setRequestId(UUID.randomUUID().toString());
            CustomResponseMetaContextHolder.setApiVersion(extractApiVersion(request));
            CustomResponseMetaContextHolder.setStartTime(System.currentTimeMillis());
            CustomResponseMetaContextHolder.setClientInfo(clientInfoExtractor.extract(request));

            // [2] 비즈니스 로직 실행
            filterChain.doFilter(request, response);

        } finally {
            // [3] 메모리 누수 방지
            CustomResponseMetaContextHolder.clear();
        }
    }

    private String extractApiVersion(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path != null && path.matches("^/api/v\\d+.*")) {
            String[] parts = path.split("/");
            if (parts.length > 2) {
                return parts[2];
            }
        }
        return "v1";
    }
}
