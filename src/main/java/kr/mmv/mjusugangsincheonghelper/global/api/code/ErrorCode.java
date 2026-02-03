package kr.mmv.mjusugangsincheonghelper.global.api.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // ===== Global Errors =====
    GLOBAL_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_001", "서버 내부 오류"),
    GLOBAL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_002", "잘못된 요청입니다"),
    GLOBAL_NOT_FOUND(HttpStatus.NOT_FOUND, "GLOBAL_003", "리소스를 찾을 수 없습니다"),
    GLOBAL_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_004", "허용되지 않은 메서드입니다"),
    GLOBAL_UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "GLOBAL_005", "지원하지 않는 미디어 타입입니다"),
    GLOBAL_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GLOBAL_006", "입력값 검증 오류"),

    // ===== Authentication/Authorization (Security Filter) =====
    AUTH_SECURITY_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_SECURITY_001", "토큰이 만료되었습니다"),
    AUTH_SECURITY_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_SECURITY_002", "유효하지 않은 토큰입니다"),
    AUTH_SECURITY_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "AUTH_SECURITY_003", "인증이 필요합니다"),
    AUTH_SECURITY_FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "AUTH_SECURITY_004", "접근 권한이 없습니다"),
    AUTH_SECURITY_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_SECURITY_005", "잘못된 인증 정보입니다"),

    // ===== Auth (Service) =====
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_001", "사용자를 찾을 수 없습니다"),
    AUTH_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_002", "이미 존재하는 사용자입니다"),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "리프레시 토큰이 만료되었습니다"),
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_004", "리프레시 토큰을 찾을 수 없습니다"),

    // ===== MJU Univ Auth (External API) =====
    MJU_UNIV_AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "MJU_UNIV_AUTH_001", "명지대 인증 실패: 학번 또는 비밀번호를 확인해주세요"),
    MJU_UNIV_AUTH_NOT_UNDERGRADUATE(HttpStatus.FORBIDDEN, "MJU_UNIV_AUTH_002", "학부생만 이용 가능합니다"),
    MJU_UNIV_AUTH_NETWORK_ERROR(HttpStatus.BAD_GATEWAY, "MJU_UNIV_AUTH_003", "명지대 서버 연결에 실패했습니다"),
    MJU_UNIV_AUTH_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MJU_UNIV_AUTH_004", "명지대 서버 응답 처리 중 오류가 발생했습니다"),
    MJU_UNIV_AUTH_INVALID_STUDENT_ID(HttpStatus.BAD_REQUEST, "MJU_UNIV_AUTH_005", "유효하지 않은 학번 형식입니다"),
    MJU_UNIV_AUTH_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "MJU_UNIV_AUTH_008", "명지대 인증 서비스가 현재 불안정합니다"),
    MJU_UNIV_AUTH_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MJU_UNIV_AUTH_009", "알 수 없는 명지대 인증 오류가 발생했습니다"),

    // ===== Section (강의) =====
    SECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SECTION_001", "강의를 찾을 수 없습니다"),
    SECTION_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "SECTION_002", "해당 강의는 현재 이용할 수 없습니다"),

    // ==== statistic (통계) =====
    STATISTIC_DATA_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "STATISTIC_001", "통계 데이터를 불러올 수 없습니다"),

    // ===== Subscription (구독) =====
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_001", "구독 정보를 찾을 수 없습니다"),
    SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "SUB_002", "이미 구독 중인 강의입니다"),
    SUBSCRIPTION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "SUB_003", "구독 가능한 최대 개수를 초과했습니다"),

    // ===== Timetable (시간표) =====
    TIMETABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "TT_001", "시간표 정보를 찾을 수 없습니다"),
    TIMETABLE_ALREADY_EXISTS(HttpStatus.CONFLICT, "TT_002", "이미 시간표에 등록된 강의입니다"),
    TIMETABLE_CONFLICT(HttpStatus.CONFLICT, "TT_003", "시간이 겹치는 강의가 있습니다"),

    // ===== Student Device (기기) =====
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "DEVICE_001", "디바이스 정보를 찾을 수 없습니다"),
    DEVICE_ALREADY_EXISTS(HttpStatus.CONFLICT, "DEVICE_002", "이미 등록된 디바이스입니다"),
    DEVICE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "DEVICE_003", "등록 가능한 최대 기기 개수를 초과했습니다"),
    DEVICE_PLATFORM_INVALID(HttpStatus.BAD_REQUEST, "DEVICE_004", "지원하지 않는 플랫폼입니다"),

    // ===== Practice (연습) =====
    PRACTICE_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRACTICE_001", "연습 기록을 찾을 수 없습니다"),
    PRACTICE_TIME_TOO_SHORT(HttpStatus.BAD_REQUEST, "PRACTICE_002", "연습 시간이 너무 짧습니다 (매크로 의심)"),
    PRACTICE_BASKET_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PRACTICE_003", "장바구니에는 최대 10과목까지 담을 수 있습니다"),

    // ===== System =====
    SYSTEM_CRAWLER_DOWN(HttpStatus.SERVICE_UNAVAILABLE, "SYS_001", "데이터 수집 서비스가 일시적으로 중단되었습니다"),
    SYSTEM_REDIS_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "SYS_002", "캐시 서비스 연결에 실패했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

