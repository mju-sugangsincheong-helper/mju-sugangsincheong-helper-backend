package kr.mmv.mjusugangsincheonghelper.global.api.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import kr.mmv.mjusugangsincheonghelper.global.annotation.OperationErrorCodes;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @OperationErrorCodes 어노테이션을 스캔하여 Swagger UI에 에러 응답 예시 자동 생성
 */
@Component
public class ErrorResponsesOperationCustomizer implements OperationCustomizer {

    private final ObjectMapper objectMapper;

    public ErrorResponsesOperationCustomizer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        OperationErrorCodes annotation = handlerMethod.getMethodAnnotation(OperationErrorCodes.class);

        if (annotation == null) {
            return operation;
        }

        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        // 에러 코드를 상태 코드별로 그룹화
        Map<Integer, List<ErrorCode>> groupedByStatus = new LinkedHashMap<>();
        for (ErrorCode errorCode : annotation.value()) {
            int status = errorCode.getStatus().value();
            groupedByStatus.computeIfAbsent(status, k -> new ArrayList<>()).add(errorCode);
        }

        // 각 상태 코드에 대해 응답 추가
        for (Map.Entry<Integer, List<ErrorCode>> entry : groupedByStatus.entrySet()) {
            String statusCode = String.valueOf(entry.getKey());
            List<ErrorCode> errorCodes = entry.getValue();

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setDescription(errorCodes.get(0).getStatus().getReasonPhrase());

            Content content = new Content();
            MediaType mediaType = new MediaType();

            // 각 에러 코드에 대해 example 추가
            for (ErrorCode errorCode : errorCodes) {
                Example example = new Example();
                example.setDescription(errorCode.getMessage());
                example.setValue(buildExampleResponse(errorCode));
                mediaType.addExamples(errorCode.getCode(), example);
            }

            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(statusCode, apiResponse);
        }

        return operation;
    }

    private Map<String, Object> buildExampleResponse(ErrorCode errorCode) {
        Map<String, Object> response = new LinkedHashMap<>();

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", errorCode.getCode());
        error.put("message", errorCode.getMessage());
        error.put("details", null);
        response.put("error", error);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("requestId", "example-request-id");
        meta.put("apiVersion", "v1");
        meta.put("clientIp", "127.0.0.1");
        meta.put("userAgent", "Mozilla/5.0");
        meta.put("timestamp", LocalDateTime.now().toString());
        meta.put("durationMs", 10);
        response.put("meta", meta);

        return response;
    }
}
