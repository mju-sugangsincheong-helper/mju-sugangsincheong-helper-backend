package kr.mmv.mjusugangsincheonghelper.global.api.exception;

import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    private String code;
    private String message;
    private Object details;

    public static ErrorDetail of(ErrorCode errorCode) {
        return ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(null)
                .build();
    }

    public static ErrorDetail of(ErrorCode errorCode, Object details) {
        return ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(details)
                .build();
    }
}
