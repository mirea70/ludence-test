package com.test.ludence.common.error.handler;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.common.error.info.CommonErrorInfo;
import com.test.ludence.common.error.info.ErrorInfo;
import com.test.ludence.common.error.info.SystemErrorInfo;
import com.test.ludence.common.error.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unexpected error", exception);
        return createResponse(SystemErrorInfo.UNKNOWN_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> createResponse(ErrorInfo errorInfo, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                errorInfo.getCode(),
                errorInfo.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(errorInfo.getStatus()).body(response);
    }

    private String getValidationMessage(String message) {
        return message == null ? "유효하지 않은 값입니다." : message;
    }
}
