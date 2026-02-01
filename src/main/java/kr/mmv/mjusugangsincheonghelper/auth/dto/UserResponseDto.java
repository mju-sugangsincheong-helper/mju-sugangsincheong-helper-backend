package kr.mmv.mjusugangsincheonghelper.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    /**
     * 학번
     */
    private String studentId;
    
    /**
     * 이름
     */
    private String name;
    
    /**
     * 이메일
     */
    private String email;
    
    /**
     * 학과
     */
    private String department;
    
    /**
     * 학년
     */
    private String grade;
    
    /**
     * 재학 상태
     */
    private String enrollmentStatus;
    
    /**
     * 권한 (ROLE_USER, ROLE_ADMIN)
     */
    private String role;
    
    /**
     * 활성 상태
     */
    private Boolean isActive;
    
    /**
     * 가입일
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 마지막 로그인
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
}

