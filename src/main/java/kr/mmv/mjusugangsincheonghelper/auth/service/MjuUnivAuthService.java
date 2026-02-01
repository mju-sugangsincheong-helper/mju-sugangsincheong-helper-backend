package kr.mmv.mjusugangsincheonghelper.auth.service;

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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 명지대 인증 API 연동 서비스
 * 외부 의존성을 완전히 캡슐화하여 더이상 복잡해 지지 않도록 여기서 dto 까지 완전히 응집화 함
 * 
 * API 문서: https://mju-univ-auth.shinnk.mmv.kr/openapi.json
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MjuUnivAuthService {

    private final ObjectMapper objectMapper;
    
    @Value("${mju.auth.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ================================
    // Public Methods
    // ================================

    /**
     * 명지대 인증 및 학생 정보 조회
     * basic-info와 student-card를 호출하여 필요한 정보만 추출
     * 
     * @param studentId 학번
     * @param password 비밀번호
     * @return 인증된 학생 정보
     * @throws BaseException 인증 실패 또는 학부생 아닌 경우
     */
    public AuthenticatedStudent authenticate(String studentId, String password) {
        // 1. 학번 형식 검증 (60으로 시작)
        validateStudentId(studentId);

        // 2. basic-info API 호출 (학부생 검증용)
        MjuApiResponse<StudentBasicInfo> basicInfoResponse = callBasicInfoApi(studentId, password);
        validateResponse(basicInfoResponse, studentId);

        // 3. 학부생 검증 (카테고리가 "대학"이어야 함)
        StudentBasicInfo basicInfo = basicInfoResponse.getData();
        validateUndergraduate(basicInfo);

        // 4. student-card API 호출 (상세 정보)
        MjuApiResponse<StudentCard> cardResponse = callStudentCardApi(studentId, password);
        validateResponse(cardResponse, studentId);

        // 5. 필요한 정보만 추출하여 반환
        return extractAuthenticatedStudent(studentId, basicInfo, cardResponse.getData());
    }

    // ================================
    // Private Methods
    // ================================

    private void validateResponse(MjuApiResponse<?> response, String studentId) {
        if (response.isAuthSuccess()) {
            return;
        }

        String errorCode = response.getErrorCode();
        String errorMessage = response.getErrorMessage();

        log.warn("MJU Auth API error for student: {}. Code: {}, Message: {}", studentId, errorCode, errorMessage);

        if ("INVALID_CREDENTIALS_ERROR".equals(errorCode)) {
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_INVALID_CREDENTIALS);
        }

        if ("NETWORK_ERROR".equals(errorCode)) {
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_NETWORK_ERROR);
        }

        if ("ALREADY_LOGGED_IN_ERROR".equals(errorCode) || 
            "SESSION_EXPIRED_ERROR".equals(errorCode) || 
            "SERVICE_NOT_FOUND_ERROR".equals(errorCode)) {
            // 일시적인 서비스 장애로 간주
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_SERVICE_UNAVAILABLE);
        }

        // 그 외 알 수 없는 오류
        throw new BaseException(ErrorCode.MJU_UNIV_AUTH_UNKNOWN_ERROR);
    }

    private void validateStudentId(String studentId) {
        if (studentId == null || !studentId.startsWith("60")) {
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_INVALID_STUDENT_ID);
        }
    }

    private void validateUndergraduate(StudentBasicInfo basicInfo) {
        String category = basicInfo.getCategory();
        if (category == null || !category.contains("대학")) {
            log.warn("Non-undergraduate student attempted login. Category: {}", category);
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_NOT_UNDERGRADUATE);
        }
    }

    private MjuApiResponse<StudentBasicInfo> callBasicInfoApi(String studentId, String password) {
        String url = baseUrl + "/api/v1/student-basicinfo";
        return callApi(url, studentId, password, new TypeReference<MjuApiResponse<StudentBasicInfo>>() {});
    }

    private MjuApiResponse<StudentCard> callStudentCardApi(String studentId, String password) {
        String url = baseUrl + "/api/v1/student-card";
        return callApi(url, studentId, password, new TypeReference<MjuApiResponse<StudentCard>>() {});
    }

    private <T> T callApi(String url, String studentId, String password, TypeReference<T> typeReference) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            Map<String, String> body = new HashMap<>();
            body.put("user_id", studentId);
            body.put("user_pw", password);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.debug("Calling MJU Auth API: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            return objectMapper.readValue(response.getBody(), typeReference);

        } catch (RestClientException e) {
            log.error("MJU Auth API call failed: {}", e.getMessage());
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_NETWORK_ERROR);
        } catch (Exception e) {
            log.error("Failed to parse MJU Auth API response: {}", e.getMessage());
            throw new BaseException(ErrorCode.MJU_UNIV_AUTH_PARSE_ERROR);
        }
    }

    private AuthenticatedStudent extractAuthenticatedStudent(String studentId, StudentBasicInfo basicInfo, StudentCard cardInfo) {
        StudentCard.StudentProfile profile = cardInfo.getStudentProfile();
        
        return AuthenticatedStudent.builder()
                .studentId(studentId)
                .name(profile != null ? profile.getNameKorean() : "Unknown")
                .grade(basicInfo.getGrade())
                .email(cardInfo.getPersonalContact() != null ? cardInfo.getPersonalContact().getEmail() : null)
                .department(basicInfo.getDepartment())
                .enrollmentStatus(profile != null ? profile.getEnrollmentStatus() : null)
                .build();
    }

    // ================================
    // Inner DTOs (외부 API 응답용)
    // ================================

    /**
     * 인증된 학생 정보 (내부에서 사용할 DTO)
     * Student 엔티티 생성에 필요한 정보만 포함
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticatedStudent {
        private String studentId;
        private String name;
        private String grade;
        private String email;
        private String department;
        private String enrollmentStatus;
    }

    /**
     * 명지대 인증 API 공통 응답 래퍼
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MjuApiResponse<T> {

        @JsonProperty("request_succeeded")
        private Boolean requestSucceeded;

        @JsonProperty("credentials_valid")
        private Boolean credentialsValid;

        private T data;

        @JsonProperty("error_code")
        private String errorCode;

        @JsonProperty("error_message")
        private String errorMessage;

        private Boolean success;

        public boolean isAuthSuccess() {
            return Boolean.TRUE.equals(requestSucceeded) 
                    && Boolean.TRUE.equals(credentialsValid) 
                    && Boolean.TRUE.equals(success);
        }
    }

    /**
     * student-basicinfo API 응답 데이터
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class StudentBasicInfo {
        private String department;
        private String category;
        private String grade;
        
        @JsonProperty("last_access_time")
        private String lastAccessTime;
        
        @JsonProperty("last_access_ip")
        private String lastAccessIp;
        
        @JsonProperty("raw_html_data")
        private String rawHtmlData;
    }

    /**
     * student-card API 응답 데이터
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class StudentCard {

        @JsonProperty("student_profile")
        private StudentProfile studentProfile;

        @JsonProperty("personal_contact")
        private PersonalContact personalContact;

        @JsonProperty("raw_html_data")
        private String rawHtmlData;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StudentProfile {
            @JsonProperty("student_id")
            private String studentId;

            @JsonProperty("name_korean")
            private String nameKorean;

            private String grade;

            @JsonProperty("enrollment_status")
            private String enrollmentStatus;

            @JsonProperty("college_department")
            private String collegeDepartment;

            @JsonProperty("academic_advisor")
            private String academicAdvisor;

            @JsonProperty("student_designed_major_advisor")
            private String studentDesignedMajorAdvisor;

            @JsonProperty("photo_base64")
            private String photoBase64;
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PersonalContact {
            @JsonProperty("english_surname")
            private String englishSurname;

            @JsonProperty("english_givenname")
            private String englishGivenname;

            @JsonProperty("phone_number")
            private String phoneNumber;

            @JsonProperty("mobile_number")
            private String mobileNumber;

            private String email;

            @JsonProperty("current_residence_address")
            private Address currentResidenceAddress;

            @JsonProperty("resident_registration_address")
            private Address residentRegistrationAddress;
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Address {
            @JsonProperty("postal_code")
            private String postalCode;

            private String address;
        }
    }
}
