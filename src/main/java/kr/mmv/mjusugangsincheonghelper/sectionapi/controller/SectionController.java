package kr.mmv.mjusugangsincheonghelper.sectionapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import kr.mmv.mjusugangsincheonghelper.sectionapi.dto.SectionResponseDto;
import kr.mmv.mjusugangsincheonghelper.sectionapi.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 강의 API 컨트롤러
 */
@Tag(name = "Section", description = "강의 조회 API")
@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    @Operation(
            summary = "전체 강의 목록 조회",
            description = "현재 활성화된 모든 강의 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<List<SectionResponseDto>>> getAllSections() {
        List<SectionResponseDto> sections = sectionService.getAllSections();
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(sections));
    }

    @GetMapping("/{sectioncls}")
    @Operation(
            summary = "강의 상세 조회",
            description = "특정 강의의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.SECTION_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<SectionResponseDto>> getSectionById(
            @Parameter(description = "분반번호 (sectioncls)") @PathVariable String sectioncls) {
        SectionResponseDto section = sectionService.getSectionById(sectioncls);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(section));
    }

    @GetMapping("/department/{deptcd}")
    @Operation(
            summary = "학과별 강의 조회",
            description = "특정 학과의 강의 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.SECTION_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<List<SectionResponseDto>>> getSectionsByDepartment(
            @Parameter(description = "학과 코드") @PathVariable String deptcd) {
        List<SectionResponseDto> sections = sectionService.getSectionsByDepartment(deptcd);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(sections));
    }

    @GetMapping("/campus/{campusdiv}")
    @Operation(
            summary = "캠퍼스별 강의 조회",
            description = "특정 캠퍼스의 강의 목록을 조회합니다. (10: 자연, 20: 인문)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.SECTION_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<List<SectionResponseDto>>> getSectionsByCampus(
            @Parameter(description = "캠퍼스 코드 (10: 자연, 20: 인문)") @PathVariable String campusdiv) {
        List<SectionResponseDto> sections = sectionService.getSectionsByCampus(campusdiv);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(sections));
    }

    @GetMapping("/search")
    @Operation(
            summary = "강의 검색",
            description = "과목명으로 강의를 검색합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "검색 성공")
            }
    )
    @OperationErrorCodes({
            ErrorCode.SECTION_NOT_FOUND,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<List<SectionResponseDto>>> searchSections(
            @Parameter(description = "검색 키워드") @RequestParam String keyword) {
        List<SectionResponseDto> sections = sectionService.searchSections(keyword);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(sections));
    }
}
