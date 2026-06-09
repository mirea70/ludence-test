package com.test.ludence.auth.domain.info;

import com.test.ludence.common.error.info.ErrorInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorInfo implements ErrorInfo {

    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "AUTH_001", "이미 사용 중인 username입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "username 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 인증 토큰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
