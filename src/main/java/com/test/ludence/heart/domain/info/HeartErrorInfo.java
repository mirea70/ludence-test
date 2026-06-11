package com.test.ludence.heart.domain.info;

import com.test.ludence.common.error.info.ErrorInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HeartErrorInfo implements ErrorInfo {

    INVALID_REFERENCE_ID(HttpStatus.BAD_REQUEST, "HEART_001", "하트 참조 ID가 유효하지 않습니다."),
    INVALID_COUNT(HttpStatus.CONFLICT, "HEART_002", "하트 수는 0보다 작을 수 없습니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "HEART_003", "이미 하트한 포스트입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "HEART_004", "하트를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
