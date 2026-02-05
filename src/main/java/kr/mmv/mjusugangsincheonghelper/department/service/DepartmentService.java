package kr.mmv.mjusugangsincheonghelper.department.service;

import kr.mmv.mjusugangsincheonghelper.department.dto.DepartmentResponseDto;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.BaseException;
import kr.mmv.mjusugangsincheonghelper.global.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 학과 조회 서비스
 * Section 테이블에서 DISTINCT 학과 정보 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final SectionRepository sectionRepository;
    
    /** 유효한 캠퍼스 코드 */
    private static final Set<String> VALID_CAMPUS_CODES = Set.of("10", "20");

    /**
     * 전체 학과 목록 조회
     * 캐시: "departments" (TTL 5분)
     * 
     * @throws BaseException DEPARTMENT_NOT_FOUND - 학과 데이터가 없는 경우
     * @throws BaseException DEPARTMENT_DATA_UNAVAILABLE - DB 조회 실패
     */
    @Cacheable(value = "departments", key = "'all'")
    public List<DepartmentResponseDto> getAllDepartments() {
        log.debug("학과 목록 조회 - 캐시 미스, DB 조회");
        try {
            List<DepartmentResponseDto> departments = sectionRepository.findDistinctDepartments().stream()
                    .map(DepartmentResponseDto::from)
                    .collect(Collectors.toList());
            
            if (departments.isEmpty()) {
                log.warn("학과 데이터가 없습니다");
                throw new BaseException(ErrorCode.DEPARTMENT_NOT_FOUND);
            }
            
            return departments;
        } catch (BaseException e) {
            throw e; // 이미 처리된 예외는 그대로 던짐
        } catch (Exception e) {
            log.error("학과 목록 조회 중 오류 발생", e);
            throw new BaseException(ErrorCode.DEPARTMENT_DATA_UNAVAILABLE);
        }
    }

    /**
     * 캠퍼스별 학과 목록 조회
     * 캐시: "departments" (TTL 5분)
     * 
     * @param campusdiv 캠퍼스 코드 (10: 자연, 20: 인문)
     * @throws BaseException DEPARTMENT_INVALID_CAMPUS - 잘못된 캠퍼스 코드
     * @throws BaseException DEPARTMENT_NOT_FOUND - 해당 캠퍼스에 학과가 없는 경우
     * @throws BaseException DEPARTMENT_DATA_UNAVAILABLE - DB 조회 실패
     */
    @Cacheable(value = "departments", key = "#campusdiv")
    public List<DepartmentResponseDto> getDepartmentsByCampus(String campusdiv) {
        log.debug("캠퍼스별 학과 목록 조회 - campusdiv: {}", campusdiv);
        
        // 캠퍼스 코드 유효성 검증
        if (!VALID_CAMPUS_CODES.contains(campusdiv)) {
            log.warn("잘못된 캠퍼스 코드: {}", campusdiv);
            throw new BaseException(ErrorCode.DEPARTMENT_INVALID_CAMPUS);
        }
        
        try {
            List<DepartmentResponseDto> departments = sectionRepository.findDistinctDepartmentsByCampus(campusdiv).stream()
                    .map(DepartmentResponseDto::from)
                    .collect(Collectors.toList());
            
            if (departments.isEmpty()) {
                log.warn("캠퍼스 {}에 해당하는 학과가 없습니다", campusdiv);
                throw new BaseException(ErrorCode.DEPARTMENT_NOT_FOUND);
            }
            
            return departments;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("캠퍼스별 학과 목록 조회 중 오류 발생 - campusdiv: {}", campusdiv, e);
            throw new BaseException(ErrorCode.DEPARTMENT_DATA_UNAVAILABLE);
        }
    }
}
