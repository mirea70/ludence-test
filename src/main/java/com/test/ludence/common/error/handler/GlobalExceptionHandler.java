package com.test.ludence.common.error.handler;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.common.error.info.CommonErrorInfo;
import com.test.ludence.common.error.info.ErrorInfo;
import com.test.ludence.common.error.info.SystemErrorInfo;
import com.test.ludence.common.error.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException exception,
            HttpServletRequest request
    ) {
        return createResponse(exception.getErrorInfo(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return createResponse(exception.getErrorInfo(), request);
    }

    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(
            BindException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), getValidationMessage(error.getDefaultMessage())));

        ErrorInfo errorInfo = CommonErrorInfo.INVALID_REQUEST;
        ErrorResponse response = new ErrorResponse(
                clock,
                errorInfo.getCode(),
                errorInfo.getMessage(),
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(errorInfo.getStatus()).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return createResponse(CommonErrorInfo.RESOURCE_NOT_FOUND, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return createResponse(CommonErrorInfo.INVALID_REQUEST, request);
    }

    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<ErrorResponse> handleTaskRejectedException(
            TaskRejectedException exception,
            HttpServletRequest request
    ) {
        ErrorInfo errorInfo = SystemErrorInfo.CAPACITY_EXCEEDED;
        ErrorResponse response = createErrorResponse(errorInfo, request);
        return ResponseEntity.status(errorInfo.getStatus())
                .header(HttpHeaders.RETRY_AFTER, "1")
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unexpected error", exception);
        return createResponse(SystemErrorInfo.UNKNOWN_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> createResponse(ErrorInfo errorInfo, HttpServletRequest request) {
        return ResponseEntity.status(errorInfo.getStatus()).body(createErrorResponse(errorInfo, request));
    }

    private ErrorResponse createErrorResponse(ErrorInfo errorInfo, HttpServletRequest request) {
        return new ErrorResponse(
                clock,
                errorInfo.getCode(),
                errorInfo.getMessage(),
                request.getRequestURI()
        );
    }

    private String getValidationMessage(String message) {
        return message == null ? "유효하지 않은 값입니다." : message;
    }
}
