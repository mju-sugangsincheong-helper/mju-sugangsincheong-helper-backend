package kr.mmv.mjusugangsincheonghelper.department.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.mmv.mjusugangsincheonghelper.department.dto.DepartmentResponseDto;
import kr.mmv.mjusugangsincheonghelper.department.service.DepartmentService;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.SingleSuccessResponseEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 학과 API 컨트롤러
 * Section 테이블에서 존재하는 학과 목록 조회
 */
@Tag(name = "Department", description = "학과 조회 API")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(
            summary = "전체 학과 목록 조회",
            description = "현재 활성화된 강의에 존재하는 모든 학과 목록을 조회합니다. (캐시 적용: 5분)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "학과 데이터 없음"),
                    @ApiResponse(responseCode = "503", description = "서비스 일시 불가")
            }
    )
    @OperationErrorCodes({
            ErrorCode.DEPARTMENT_NOT_FOUND,
            ErrorCode.DEPARTMENT_DATA_UNAVAILABLE,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<List<DepartmentResponseDto>>> getAllDepartments() {
        List<DepartmentResponseDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(departments));
    }

    @GetMapping("/campus/{campusdiv}")
    @Operation(
            summary = "캠퍼스별 학과 목록 조회",
            description = "특정 캠퍼스의 학과 목록을 조회합니다. (10: 자연캠퍼스, 20: 인문캠퍼스)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 캠퍼스 코드"),
                    @ApiResponse(responseCode = "404", description = "학과 데이터 없음"),
                    @ApiResponse(responseCode = "503", description = "서비스 일시 불가")
            }
    )
    @OperationErrorCodes({
            ErrorCode.DEPARTMENT_INVALID_CAMPUS,
            ErrorCode.DEPARTMENT_NOT_FOUND,
            ErrorCode.DEPARTMENT_DATA_UNAVAILABLE,
            ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<List<DepartmentResponseDto>>> getDepartmentsByCampus(
            @Parameter(description = "캠퍼스 코드 (10: 자연, 20: 인문)") @PathVariable String campusdiv) {
        List<DepartmentResponseDto> departments = departmentService.getDepartmentsByCampus(campusdiv);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(departments));
    }
}
