package kr.mmv.mjusugangsincheonghelper.auth.service;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.mmv.mjusugangsincheonghelper.auth.dto.*;
import kr.mmv.mjusugangsincheonghelper.auth.security.JwtTokenProvider;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.BaseException;
import kr.mmv.mjusugangsincheonghelper.global.entity.Student;
import kr.mmv.mjusugangsincheonghelper.global.entity.StudentPrivacy;
import kr.mmv.mjusugangsincheonghelper.global.repository.PracticeSessionRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentDeviceRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.SubscriptionRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.TimetableRepository;

/**
 * 인증 서비스
 * MjuUnivAuthService를 통한 인증 및 JWT 토큰 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final StudentRepository studentRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MjuUnivAuthService mjuUnivAuthService;
    private final SubscriptionRepository subscriptionRepository;
    private final PracticeSessionRepository practiceSessionRepository;
    private final TimetableRepository timetableRepository;
    private final StudentDeviceRepository studentDeviceRepository;

    @Value("${app.jwt.expiration:3600000}")
    private long expirationMs;

    /**
     * 인증 (로그인/회원가입 통합)
     * 1. 명지대 API로 인증
     * 2. DB 확인 (Phase 1/2/3 분기)
     * 3. JWT 토큰 발급
     */
    @Transactional
    public TokenResponseDto authenticate(AuthRequestDto request, String deviceInfo) {
        // 1. 명지대 인증 API 호출
        MjuUnivAuthService.AuthenticatedStudent authResult = 
                mjuUnivAuthService.authenticate(request.getUserId(), request.getUserPw());

        Student student = studentRepository.findById(authResult.getStudentId()).orElse(null);

        if (student == null) {
            // [신규 사용자 시나리오]
            if (request.getIsAgreed() == null || !request.getIsAgreed()) {
                // Phase 1: 가입 여부 탐색 (동의 미포함 시 에러 반환)
                log.info("New user detected, requesting privacy consent: {}", authResult.getStudentId());
                throw new BaseException(ErrorCode.AUTH_PRIVACY_POLICY_NOT_AGREED);
            }

            // Phase 2: 회원가입 및 첫 로그인 (동의 포함 시 생성)
            student = createNewStudentWithPrivacy(authResult, deviceInfo);
        } else {
            // [기존 사용자 시나리오]
            // Phase 3: 기존 사용자 로그인 및 정보 동기화
            updateStudentInfo(student, authResult);
        }

        // 4. JWT 토큰 생성 및 갱신
        String accessToken = jwtTokenProvider.generateToken(student.getStudentId(), student.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(student.getStudentId());

        student.updateTokenInfo(refreshToken);
        studentRepository.save(student);

        log.info("User authenticated: {} ({})", student.getStudentId(), student.getName());

        return TokenResponseDto.of(accessToken, refreshToken, expirationMs / 1000);
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public TokenResponseDto refreshToken(String refreshToken) {
        // JWT 토큰 유효성 검증
        try {
            jwtTokenProvider.validateToken(refreshToken);
        } catch (ExpiredJwtException e) {
            log.warn("Refresh token expired: {}", e.getMessage());
            throw new BaseException(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        } catch (Exception e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            throw new BaseException(ErrorCode.AUTH_SECURITY_INVALID_TOKEN);
        }

        String studentId = jwtTokenProvider.getUsername(refreshToken);
        
        // DB에서 사용자 조회 및 Refresh Token 검증
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        // DB에 저장된 토큰과 비교
        if (!refreshToken.equals(student.getRefreshToken())) {
            log.warn("Refresh token mismatch for user: {}", studentId);
            throw new BaseException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND);
        }

        // 토큰 유효성 확인
        if (!student.isTokenValid()) {
            log.warn("Refresh token invalid for user: {}", studentId);
            throw new BaseException(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.generateToken(student.getStudentId(), student.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(student.getStudentId());

        // DB 업데이트
        student.updateTokenInfo(newRefreshToken);
        studentRepository.save(student);

        log.info("Token refreshed for user: {}", studentId);

        return TokenResponseDto.of(newAccessToken, newRefreshToken, expirationMs / 1000);
    }

    /**
     * 로그아웃 (Refresh Token 삭제)
     */
    @Transactional
    public void logout(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        student.clearToken();
        studentRepository.save(student);

        log.info("User logged out: {}", studentId);
    }

    /**
     * 회원 탈퇴
     * 1. 계정 비활성화 (is_active = false)
     * 2. Refresh Token 삭제
     * 3. 구독 목록 삭제
     * 4. 디바이스 목록 삭제
     */
    @Transactional
    public void withdraw(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        // 1. 디바이스 목록 삭제
        studentDeviceRepository.deleteByStudentStudentId(studentId);

        // 2. 구독 목록 삭제
        subscriptionRepository.deleteByUser(student);

        // 3. 연습 기록 삭제
        practiceSessionRepository.deleteByStudent(student);

        // 4. 시간표 삭제
        timetableRepository.deleteByUser(student);

        // 5. 사용자 정보 완전 삭제 (Hard Delete)
        studentRepository.delete(student);

        log.info("User completely deleted: {}", studentId);
    }

    /**
     * 현재 사용자 정보 조회
     */
    public UserResponseDto getCurrentUser(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        return toUserResponse(student);
    }

    // ===== Private Methods =====

    /**
     * 신규 사용자 및 개인정보 동의 생성
     */
    private Student createNewStudentWithPrivacy(MjuUnivAuthService.AuthenticatedStudent authResult, String deviceInfo) {
        Student newStudent = Student.builder()
                .studentId(authResult.getStudentId())
                .name(authResult.getName())
                .grade(authResult.getGrade())
                .email(authResult.getEmail())
                .department(authResult.getDepartment())
                .enrollmentStatus(authResult.getEnrollmentStatus())
                .role("ROLE_USER")
                .isActive(true)
                .build();

        // 개인정보 동의 항목                                                                                                                                                           │
        String privacyData = String.format("이름, 학년, 학과, 이메일, 기기정보"); 

        StudentPrivacy privacy = StudentPrivacy.builder()
                .data(privacyData)
                .isAgreed(true)
                .build();

        newStudent.setPrivacy(privacy);

        Student savedStudent = studentRepository.save(newStudent);
        log.info("New student signed up: {} ({})", savedStudent.getStudentId(), savedStudent.getName());

        return savedStudent;
    }

    /**
     * 기존 사용자 정보 업데이트
     */
    private Student updateStudentInfo(Student student, MjuUnivAuthService.AuthenticatedStudent authResult) {
        student.setName(authResult.getName());
        student.setGrade(authResult.getGrade());
        student.setEmail(authResult.getEmail());
        student.setDepartment(authResult.getDepartment());
        student.setEnrollmentStatus(authResult.getEnrollmentStatus());

        return student;
    }

    /**
     * Student -> UserResponseDto 변환
     */
    private UserResponseDto toUserResponse(Student student) {
        return UserResponseDto.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .email(student.getEmail())
                .department(student.getDepartment())
                .grade(student.getGrade())
                .enrollmentStatus(student.getEnrollmentStatus())
                .role(student.getRole())
                .isActive(student.getIsActive())
                .createdAt(student.getCreatedAt())
                .lastLoginAt(student.getLastLoginAt())
                .build();
    }
}
