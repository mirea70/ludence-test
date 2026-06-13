package com.test.ludence.common.error.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SystemErrorInfo implements ErrorInfo {

    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_001", "알 수 없는 시스템 에러가 발생하였습니다."),
    CAPACITY_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "SYSTEM_002", "시스템이 한계에 도달했습니다. 잠시 후 재시도 해주세요."),
    UPLOAD_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "SYSTEM_003", "업로드할 수 있는 파일 최대 크기를 초과하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
