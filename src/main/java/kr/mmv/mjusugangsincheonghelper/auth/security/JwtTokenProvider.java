package kr.mmv.mjusugangsincheonghelper.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration:3600000}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 생성 (단일 role)
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰에서 role 추출 (단일 값)
     */
    public String getRole(String token) {
        Claims claims = getClaims(token);
        Object role = claims.get("role");
        if (role instanceof String) {
            return (String) role;
        }
        return "ROLE_USER";
    }

    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        String role = getRole(token);

        // role이 이미 ROLE_ 접두사를 포함하고 있으면 그대로 사용
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(authority)
        );

        UserDetails userDetails = User.builder()
                .username(username)
                .password("")
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
