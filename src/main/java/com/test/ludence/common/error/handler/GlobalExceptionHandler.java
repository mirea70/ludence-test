package com.test.ludence.common.error.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.common.error.info.CommonErrorInfo;
import com.test.ludence.common.error.info.ErrorInfo;
import com.test.ludence.common.error.info.SystemErrorInfo;
import com.test.ludence.common.error.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;
    private final MultipartProperties multipartProperties;

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

    @ExceptionHandler({BindException.class, org.springframework.web.bind.MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(
            BindException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), getValidationMessage(error.getDefaultMessage())));

        return createResponse(CommonErrorInfo.INVALID_REQUEST, request, details);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
            MissingRequestHeaderException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> details = Map.of("header", exception.getHeaderName());
        return createResponse(CommonErrorInfo.INVALID_REQUEST, request, details);
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
        if (exception.getRequiredType() != null && exception.getRequiredType().isEnum()) {
            return createResponse(
                    CommonErrorInfo.INVALID_REQUEST,
                    request,
                    buildEnumDetails(exception.getName(), exception.getValue(), exception.getRequiredType())
            );
        }

        return createResponse(CommonErrorInfo.INVALID_REQUEST, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        InvalidFormatException invalidFormatException = findInvalidFormatException(exception);
        if (invalidFormatException == null) {
            return createResponse(CommonErrorInfo.INVALID_REQUEST, request);
        }

        String fieldName = invalidFormatException.getPath().isEmpty()
                ? "unknown"
                : invalidFormatException.getPath().get(0).getFieldName();

        if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
            return createResponse(
                    CommonErrorInfo.INVALID_REQUEST,
                    request,
                    buildEnumDetails(fieldName, invalidFormatException.getValue(), invalidFormatException.getTargetType())
            );
        }

        String typeName = invalidFormatException.getTargetType() != null
                ? invalidFormatException.getTargetType().getSimpleName()
                : "알 수 없는 타입";
        Map<String, Object> details = Map.of(
                fieldName,
                String.format("'%s' 값은 %s 타입으로 변환할 수 없습니다.", invalidFormatException.getValue(), typeName)
        );
        return createResponse(CommonErrorInfo.INVALID_REQUEST, request, details);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        HttpHeaders headers = new HttpHeaders();
        if (exception.getSupportedHttpMethods() != null) {
            headers.setAllow(exception.getSupportedHttpMethods());
        }

        return ResponseEntity.status(CommonErrorInfo.METHOD_NOT_ALLOWED.getStatus())
                .headers(headers)
                .body(new ErrorResponse(clock, CommonErrorInfo.METHOD_NOT_ALLOWED, request));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return createResponse(CommonErrorInfo.UNSUPPORTED_MEDIA_TYPE, request);
    }

    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<ErrorResponse> handleTaskRejectedException(
            TaskRejectedException exception,
            HttpServletRequest request
    ) {
        ErrorInfo errorInfo = SystemErrorInfo.CAPACITY_EXCEEDED;
        ErrorResponse response = new ErrorResponse(clock, errorInfo, request);
        return ResponseEntity.status(errorInfo.getStatus())
                .header(HttpHeaders.RETRY_AFTER, "1")
                .body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> details = Map.of("maxFileSize", multipartProperties.getMaxFileSize().toString());
        return createResponse(SystemErrorInfo.UPLOAD_SIZE_EXCEEDED, request, details);
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
        return ResponseEntity.status(errorInfo.getStatus()).body(new ErrorResponse(clock, errorInfo, request));
    }

    private ResponseEntity<ErrorResponse> createResponse(
            ErrorInfo errorInfo,
            HttpServletRequest request,
            Map<String, Object> details
    ) {
        return ResponseEntity.status(errorInfo.getStatus()).body(new ErrorResponse(clock, errorInfo, request, details));
    }

    private InvalidFormatException findInvalidFormatException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof InvalidFormatException invalidFormatException) {
                return invalidFormatException;
            }
            current = current.getCause();
        }
        return null;
    }

    private Map<String, Object> buildEnumDetails(String name, Object invalidValue, Class<?> enumType) {
        String allowedValues = Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));

        String detailMessage = String.format("'%s' 값은 유효하지 않습니다. 허용되는 값: %s", invalidValue, allowedValues);
        return Map.of(name, detailMessage);
    }

    private String getValidationMessage(String message) {
        return message == null ? "유효하지 않은 값이 존재합니다." : message;
    }
}
