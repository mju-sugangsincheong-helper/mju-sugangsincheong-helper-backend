package kr.mmv.mjusugangsincheonghelper.auth.service;

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
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.SubscriptionRepository;

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

    @Value("${jwt.expiration:1209600000}")
    private long expirationMs;  // 14일

    /**
     * 인증 (로그인/회원가입 통합)
     * 1. 명지대 API로 인증 (MjuUnivAuthService 위임)
     * 2. DB에 없으면 자동 생성
     * 3. JWT 토큰 발급
     */
    @Transactional
    public TokenResponseDto authenticate(AuthRequestDto request) {
        // 1. 명지대 인증 API 호출 (MjuUnivAuthService에 위임)
        MjuUnivAuthService.AuthenticatedStudent authResult = 
                mjuUnivAuthService.authenticate(request.getUserId(), request.getUserPw());

        // 2. DB에서 사용자 조회 또는 생성
        Student student = studentRepository.findById(authResult.getStudentId())
                .map(existingStudent -> updateStudentInfo(existingStudent, authResult))
                .orElseGet(() -> createNewStudent(authResult));

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(student.getStudentId(), student.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(student.getStudentId());

        // 4. Refresh Token DB 저장
        student.updateTokenInfo(refreshToken);
        studentRepository.save(student);

        log.info("User authenticated: {} ({})", student.getStudentId(), student.getName());

        return TokenResponseDto.of(accessToken, refreshToken, expirationMs / 1000);
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public TokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        String refreshToken = request.getRefreshToken();

        // JWT 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
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

        // 1. 계정 비활성화 & 2. 토큰 삭제
        student.deactivate();
        student.clearToken();

        // 3. 디바이스 목록 삭제 (orphanRemoval = true 이용)
        student.getDevices().clear();

        // 4. 구독 목록 삭제
        subscriptionRepository.deleteByUser(student);

        // 변경사항 저장 (디바이스 삭제도 여기서 flush 되면서 반영됨)
        studentRepository.save(student);

        log.info("User withdrew: {}", studentId);
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
     * 신규 사용자 생성
     */
    private Student createNewStudent(MjuUnivAuthService.AuthenticatedStudent authResult) {
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

        Student savedStudent = studentRepository.save(newStudent);
        log.info("New student created: {} ({})", savedStudent.getStudentId(), savedStudent.getName());

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
