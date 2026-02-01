package kr.mmv.mjusugangsincheonghelper.global.annotation;

import java.lang.annotation.*;

/**
 * API 버전을 명시하는 어노테이션
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {
    String value() default "v1";
}
