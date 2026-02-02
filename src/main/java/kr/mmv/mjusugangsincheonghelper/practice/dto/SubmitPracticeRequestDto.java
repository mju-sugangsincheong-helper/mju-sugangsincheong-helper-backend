package kr.mmv.mjusugangsincheonghelper.practice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수강신청 연습 결과 제출 요청
 */
@Getter
@NoArgsConstructor
public class SubmitPracticeRequestDto {

    @NotNull(message = "총 소요 시간은 필수입니다")
    @Min(value = 0, message = "소요 시간은 음수일 수 없습니다")
    private Long totalTimeMs;

    @NotNull(message = "장바구니 과목 수는 필수입니다")
    @Min(value = 1, message = "장바구니 과목 수는 최소 1개 이상이어야 합니다")
    private Integer countNum;
}
