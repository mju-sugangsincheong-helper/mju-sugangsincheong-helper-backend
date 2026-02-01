package kr.mmv.mjusugangsincheonghelper.global.annotation;

import java.lang.annotation.*;

import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;

/**
 * API 문서에서 발생 가능한 에러 코드를 선언하는 어노테이션
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationErrorCodes {
    ErrorCode[] value();
}
