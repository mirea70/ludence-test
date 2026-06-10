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
    ALREADY_DELETED(HttpStatus.CONFLICT, "POST_004", "이미 삭제된 포스트입니다."),
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "POST_005", "PNG 형식의 이미지가 아닙니다."),
    IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "POST_006", "이미지는 최대 2 MiB입니다."),
    IMAGE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "POST_007", "이미지 저장에 실패했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "POST_008", "포스트를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_009", "포스트 이미지를 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "POST_010", "포스트 작성자만 수행할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
