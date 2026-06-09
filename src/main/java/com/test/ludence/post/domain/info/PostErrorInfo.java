package com.test.ludence.post.domain.info;

import com.test.ludence.common.error.info.ErrorInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorInfo implements ErrorInfo {

    INVALID_AUTHOR_ID(HttpStatus.BAD_REQUEST, "POST_001", "작성자 ID가 유효하지 않습니다."),
    INVALID_TITLE(HttpStatus.BAD_REQUEST, "POST_002", "포스트 제목이 유효하지 않습니다."),
    INVALID_DESCRIPTION(HttpStatus.BAD_REQUEST, "POST_003", "포스트 설명이 유효하지 않습니다."),
    ALREADY_DELETED(HttpStatus.CONFLICT, "POST_004", "이미 삭제된 포스트입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
