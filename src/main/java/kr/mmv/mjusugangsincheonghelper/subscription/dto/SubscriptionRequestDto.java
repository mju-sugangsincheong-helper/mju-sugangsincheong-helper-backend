package kr.mmv.mjusugangsincheonghelper.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequestDto {

    @NotBlank(message = "분반번호(sectioncls)는 필수입니다")
    private String sectioncls;
}
