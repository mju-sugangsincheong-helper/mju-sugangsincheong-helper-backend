package kr.mmv.mjusugangsincheonghelper.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.BaseException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 명지대 인증 API 연동 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MjuUnivAuthService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.mju-univ-auth.base-url}")
    private String baseUrl;

    private static final String PATH_BASIC_INFO = "/api/v1/student-basicinfo";
    private static final String PATH_STUDENT_CARD = "/api/v1/student-card";

    public AuthenticatedStudent authenticate(String studentId, String password) {
        validateInput(studentId);

        // 1. 기초 정보 조회
        MjuApiResponse<StudentBasicInfo> basicRes = exchange(PATH_BASIC_INFO, studentId, password, new TypeReference<>() {});
        validateUndergraduate(basicRes.getData());

        // 2. 학생증 정보 조회
        MjuApiResponse<StudentCard> cardRes = exchange(PATH_STUDENT_CARD, studentId, password, new TypeReference<>() {});

        return mapToAuthenticatedStudent(studentId, basicRes.getData(), cardRes.getData());
    }

    /**
     * 외부 API 통신 메서드
     */
    private <T> MjuApiResponse<T> exchange(String path, String studentId, String password, TypeReference<MjuApiResponse<T>> typeRef) {
        String url = baseUrl + path;
        
        try {
            HttpEntity<Map<String, Object>> entity = createEntity(studentId, password);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            return parseSuccess(response.getBody(), typeRef);

        } catch (HttpStatusCodeException e) {
            // 4xx, 5xx 에러 발생 시: JsonNode로 직접 파싱하여 에러 코드 추출
            String responseBody = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
            // 보안 취약점: 응답 바디 전체를 로깅하면 민감 정보(PW 등)가 노출될 수 있음
            // log.warn("[MJU-Auth] HTTP Error {}: {}", e.getStatusCode(), responseBody);
            
            String errorCode = extractErrorCode(responseBody);
            String errorMessage = extractErrorMessage(responseBody);
            log.warn("[MJU-Auth] HTTP Error {}: code={}, message={}", e.getStatusCode(), errorCode, errorMessage);

            // 401 Unauthorized인 경우 에러 코드가 없어도 인증 실패로 간주
            if (errorCode == null && e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                errorCode = "INVALID_CREDENTIALS_ERROR";
            }
            
            if (errorCode != null) {
                throw convertToDomainException(errorCode, errorMessage);
            }

            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_SERVICE_UNAVAILABLE);
            
        } catch (RestClientException e) {
            log.error("[MJU-Auth] Network Connection Error: {}", e.getMessage());
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_NETWORK_ERROR);
        }
    }

    private String extractErrorCode(String jsonBody) {
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            if (root.has("error_code") && !root.get("error_code").isNull()) {
                return root.get("error_code").asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractErrorMessage(String jsonBody) {
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            if (root.has("error_message") && !root.get("error_message").isNull()) {
                return root.get("error_message").asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private BaseException convertToDomainException(String code, String message) {
        log.info("[MJU-Auth] Business Error Found: {} ({})", code, message);

        switch (code) {
            case "INVALID_CREDENTIALS_ERROR":
                return new BaseException(ErrorCode.MJU_UNIV_AUTH_INVALID_CREDENTIALS);
                
            case "NETWORK_ERROR":
            case "SERVICE_UNKNOWN_ERROR":
                return new BaseException(ErrorCode.MJU_UNIV_AUTH_NETWORK_ERROR);
                
            case "ALREADY_LOGGED_IN_ERROR":
            case "SESSION_EXPIRED_ERROR":
            case "SERVICE_NOT_FOUND_ERROR":
            case "INVALID_SERVICE_USAGE_ERROR":
                return new BaseException(ErrorCode.MJU_UNIV_AUTH_SERVICE_UNAVAILABLE);
                
            case "PARSING_ERROR":
            case "UNKNOWN_ERROR":
            default:
                return new BaseException(ErrorCode.MJU_UNIV_AUTH_UNKNOWN_ERROR);
        }
    }

    // ================================
    // Helper Methods
    // ================================

    private <T> MjuApiResponse<T> parseSuccess(String body, TypeReference<MjuApiResponse<T>> typeRef) {
        try {
            MjuApiResponse<T> res = objectMapper.readValue(body, typeRef);
            // 200 OK를 받았는데 논리적 에러 코드가 있는 경우 체크
            if (res != null && res.getErrorCode() != null) {
                throw convertToDomainException(res.getErrorCode(), res.getErrorMessage());
            }
            return res;
        } catch (BaseException be) {
            throw be;
        } catch (Exception e) {
            log.error("[MJU-Auth] JSON Parse Error (Success Path): {}", e.getMessage());
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_PARSE_ERROR);
        }
    }


    private void validateInput(String studentId) {
        if (studentId == null || !studentId.startsWith("60")) {
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_INVALID_STUDENT_ID);
        }
    }

    private void validateUndergraduate(StudentBasicInfo data) {
        if (data == null || data.getCategory() == null || !data.getCategory().contains("대학")) {
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_NOT_UNDERGRADUATE);
        }
    }

    private HttpEntity<Map<String, Object>> createEntity(String id, String pw) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", id);
        body.put("user_pw", pw);
        return new HttpEntity<>(body, headers);
    }

    private AuthenticatedStudent mapToAuthenticatedStudent(String id, StudentBasicInfo basic, StudentCard card) {
        StudentCard.StudentProfile profile = card.getStudentProfile();
        return AuthenticatedStudent.builder()
                .studentId(id)
                .name(profile != null ? profile.getNameKorean() : "Unknown")
                .grade(basic.getGrade())
                .email(card.getPersonalContact() != null ? card.getPersonalContact().getEmail() : null)
                .department(basic.getDepartment())
                .enrollmentStatus(profile != null ? profile.getEnrollmentStatus() : null)
                .build();
    }

    // ================================
    // DTOs
    // ================================

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthenticatedStudent {
        private String studentId;
        private String name;
        private String grade;
        private String email;
        private String department;
        private String enrollmentStatus;
    }

    // 성공 응답용 (제네릭)
    @Data @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MjuApiResponse<T> {
        @JsonProperty("request_succeeded") private boolean requestSucceeded;
        @JsonProperty("credentials_valid") private boolean credentialsValid;
        @JsonProperty("error_code") private String errorCode; // 200 OK여도 있을 수 있음
        @JsonProperty("error_message") private String errorMessage;
        private boolean success;
        private T data;
    }

    // 실패 응답용 (단순형) - 제네릭 T 타입 문제 회피
    @Data @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MjuErrorResponse {
        @JsonProperty("error_code") private String errorCode;
        @JsonProperty("error_message") private String errorMessage;
    }

    @Data @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    private static class StudentBasicInfo {
        private String department;
        private String category;
        private String grade;
    }

    @Data @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    private static class StudentCard {
        @JsonProperty("student_profile") private StudentProfile studentProfile;
        @JsonProperty("personal_contact") private PersonalContact personalContact;

        @Data @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StudentProfile {
            @JsonProperty("name_korean") private String nameKorean;
            @JsonProperty("enrollment_status") private String enrollmentStatus;
        }

        @Data @NoArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PersonalContact {
            private String email;
        }
    }
}