package com.test.ludence.common.error.response;

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
}
