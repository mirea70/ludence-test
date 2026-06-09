package com.test.ludence.common.error.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorInfo implements ErrorInfo {

    INVALID_PAGE(HttpStatus.BAD_REQUEST, "COMMON_001", "페이지 요청 값이 유효하지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_002", "요청한 리소스를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_003", "요청 정보 중 유효하지 않은 값이 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
