package kr.mmv.mjusugangsincheonghelper.auth.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.mmv.mjusugangsincheonghelper.auth.security.JwtTokenProvider;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set Authentication to security context for '{}', uri: {}",
                            authentication.getName(), request.getRequestURI());
                }
            } catch (ExpiredJwtException e) {
                log.warn("Expired JWT token: {}", e.getMessage());
                request.setAttribute("jwt_error_code", ErrorCode.AUTH_SECURITY_EXPIRED_TOKEN);
            } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                request.setAttribute("jwt_error_code", ErrorCode.AUTH_SECURITY_INVALID_TOKEN);
            } catch (Exception e) {
                log.error("JWT authentication error: {}", e.getMessage());
                request.setAttribute("jwt_error_code", ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
