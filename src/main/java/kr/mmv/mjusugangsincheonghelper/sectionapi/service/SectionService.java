package kr.mmv.mjusugangsincheonghelper.sectionapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.BaseException;
import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import kr.mmv.mjusugangsincheonghelper.global.repository.SectionRepository;
import kr.mmv.mjusugangsincheonghelper.sectionapi.dto.SectionResponseDto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 강의 조회 서비스
 * PK: sectioncls (String)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SectionService {

    private final SectionRepository sectionRepository;

    /**
     * 전체 강의 목록 조회 (활성화된 것만)
     */
    public List<SectionResponseDto> getAllSections() {
        return sectionRepository.findByIsActiveTrue().stream()
                .map(SectionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 강의 sectioncls로 조회
     */
    public SectionResponseDto getSectionById(String sectioncls) {
        Section section = sectionRepository.findById(sectioncls)
                .orElseThrow(() -> new BaseException(ErrorCode.SECTION_NOT_FOUND));
        return SectionResponseDto.from(section);
    }

    /**
     * 학과별 강의 조회
     */
    public List<SectionResponseDto> getSectionsByDepartment(String deptcd) {
        return sectionRepository.findByDeptcdAndIsActiveTrue(deptcd).stream()
                .map(SectionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 캠퍼스별 강의 조회
     */
    public List<SectionResponseDto> getSectionsByCampus(String campusdiv) {
        return sectionRepository.findByCampusdivAndIsActiveTrue(campusdiv).stream()
                .map(SectionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 검색 (과목명)
     */
    public List<SectionResponseDto> searchSections(String keyword) {
        return sectionRepository.searchByCurinm(keyword).stream()
                .map(SectionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 검색 (교수명)
     */
    public List<SectionResponseDto> searchByProfessor(String keyword) {
        return sectionRepository.searchByProfnm(keyword).stream()
                .map(SectionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 여석 있는 강의 조회
     */
    public List<SectionResponseDto> getAvailableSections() {
        return sectionRepository.findAvailableSections().stream()
                .map(SectionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 학수번호로 강의 조회
     */
    public List<SectionResponseDto> getSectionsByCurinum(String curinum) {
        return sectionRepository.findByCurinumAndIsActiveTrue(curinum).stream()
                .map(SectionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Entity 조회 (내부용)
     */
    public Section getSectionEntity(String sectioncls) {
        return sectionRepository.findById(sectioncls)
                .orElseThrow(() -> new BaseException(ErrorCode.SECTION_NOT_FOUND));
    }
}
