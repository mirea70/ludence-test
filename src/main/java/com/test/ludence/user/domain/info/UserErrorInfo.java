package com.test.ludence.user.domain.info;

import com.test.ludence.common.error.info.ErrorInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorInfo implements ErrorInfo {

    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "USER_001", "username 형식이 유효하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_002", "암호화된 비밀번호가 비어 있을 수 없습니다."),
    ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "USER_003", "이미 탈퇴한 회원입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "USER_004", "회원을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
