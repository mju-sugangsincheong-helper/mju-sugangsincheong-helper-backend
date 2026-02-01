package kr.mmv.mjusugangsincheonghelper.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.ErrorResponseEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 필터 체인 예외는 @RestControllerAdvice 도달 전 발생
 * 필터 레벨에서 직접 ErrorResponseEnvelope 생성 후 직렬화
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("Authentication failed: {} - URI: {}", authException.getMessage(), request.getRequestURI());

        ErrorCode errorCode = resolveErrorCode(request, authException);
        ErrorResponseEnvelope error = ErrorResponseEnvelope.of(errorCode);

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), error);
    }

    private ErrorCode resolveErrorCode(HttpServletRequest request, AuthenticationException authException) {
        // 요청 속성에서 에러 코드 확인 (JwtAuthenticationFilter에서 설정)
        Object errorCodeAttr = request.getAttribute("jwt_error_code");
        if (errorCodeAttr instanceof ErrorCode) {
            return (ErrorCode) errorCodeAttr;
        }

        String message = authException.getMessage();
        if (message != null) {
            if (message.contains("expired") || message.contains("만료")) {
                return ErrorCode.AUTH_SECURITY_EXPIRED_TOKEN;
            }
            if (message.contains("invalid") || message.contains("유효하지")) {
                return ErrorCode.AUTH_SECURITY_INVALID_TOKEN;
            }
        }

        return ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS;
    }
}
