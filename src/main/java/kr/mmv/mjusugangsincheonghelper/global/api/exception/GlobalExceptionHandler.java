package kr.mmv.mjusugangsincheonghelper.global.api.exception;

import jakarta.validation.ConstraintViolationException;
import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.envelope.ErrorResponseEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleBaseException(BaseException ex) {
        log.error("BaseException occurred: {}", ex.getErrorCode(), ex);
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseEnvelope.of(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validation failed: {}", errors);
        return ResponseEntity
                .status(ErrorCode.GLOBAL_VALIDATION_ERROR.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_VALIDATION_ERROR, errors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Binding failed: {}", errors);
        return ResponseEntity
                .status(ErrorCode.GLOBAL_VALIDATION_ERROR.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_VALIDATION_ERROR, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            errors.put(path, violation.getMessage());
        });
        log.warn("Constraint violation: {}", errors);
        return ResponseEntity
                .status(ErrorCode.GLOBAL_VALIDATION_ERROR.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_VALIDATION_ERROR, errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("파라미터 '%s'의 타입이 올바르지 않습니다", ex.getName());
        log.warn("Type mismatch: {}", errorMessage);
        return ResponseEntity
                .status(ErrorCode.GLOBAL_BAD_REQUEST.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_BAD_REQUEST, errorMessage));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Message not readable: {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.GLOBAL_BAD_REQUEST.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_BAD_REQUEST, "요청 본문을 읽을 수 없습니다"));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("No handler found: {}", ex.getRequestURL());
        return ResponseEntity
                .status(ErrorCode.GLOBAL_NOT_FOUND.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMethod());
        return ResponseEntity
                .status(ErrorCode.GLOBAL_METHOD_NOT_ALLOWED.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.AUTH_SECURITY_FORBIDDEN_ACCESS.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.AUTH_SECURITY_FORBIDDEN_ACCESS));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.AUTH_SECURITY_UNAUTHORIZED_ACCESS));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseEnvelope> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String errorMessage = String.format("필수 파라미터 '%s'가 누락되었습니다", ex.getParameterName());
        log.warn("Missing request parameter: {}", errorMessage);
        return ResponseEntity
                .status(ErrorCode.GLOBAL_BAD_REQUEST.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_BAD_REQUEST, errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseEnvelope> handleException(Exception ex) {
        log.error("Unexpected exception occurred", ex);
        return ResponseEntity
                .status(ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponseEnvelope.of(ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
