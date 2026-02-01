package kr.mmv.mjusugangsincheonghelper.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증 요청 DTO (로그인/회원가입 통합)
 * 명지대 학번과 비밀번호로 인증
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {

    @NotBlank(message = "학번을 입력해주세요")
    private String userId;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String userPw;
}
