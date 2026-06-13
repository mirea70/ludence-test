package com.test.ludence.common.error.response;

import com.test.ludence.common.error.info.ErrorInfo;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        String path,
        Map<String, Object> details
) {

    public ErrorResponse(Clock clock, String code, String message, String path) {
        this(clock.instant(), code, message, path, Map.of());
    }

    public ErrorResponse(Clock clock, String code, String message, String path, Map<String, Object> details) {
        this(clock.instant(), code, message, path, Map.copyOf(details));
    }

    public ErrorResponse(Clock clock, ErrorInfo errorInfo, HttpServletRequest request) {
        this(clock, errorInfo, request, Map.of());
    }

    public ErrorResponse(
            Clock clock,
            ErrorInfo errorInfo,
            HttpServletRequest request,
            Map<String, Object> details
    ) {
        this(clock, errorInfo.getCode(), errorInfo.getMessage(), request.getRequestURI(), details);
    }
}
