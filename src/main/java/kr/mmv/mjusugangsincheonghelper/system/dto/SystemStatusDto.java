package kr.mmv.mjusugangsincheonghelper.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 시스템 상태 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatusDto {

    /**
     * 크롤러 동작 여부
     */
    private Boolean running;

    /**
     * 마지막 업데이트 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdatedAt;

    /**
     * DB 강의 수
     */
    private Long sectionCount;

    /**
     * 서버 상태 메시지
     */
    private String message;
}
