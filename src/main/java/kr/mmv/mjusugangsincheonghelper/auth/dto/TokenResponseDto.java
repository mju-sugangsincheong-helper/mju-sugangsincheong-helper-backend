package kr.mmv.mjusugangsincheonghelper.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    
    @JsonIgnore
    private String refreshToken;
    
    private String tokenType;
    private Long expiresIn;

    public static TokenResponseDto of(String accessToken, String refreshToken, Long expiresIn) {
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
