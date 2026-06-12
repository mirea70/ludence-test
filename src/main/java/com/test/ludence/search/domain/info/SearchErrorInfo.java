package com.test.ludence.search.domain.info;

import com.test.ludence.common.error.info.ErrorInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchErrorInfo implements ErrorInfo {

    INVALID_QUERY(HttpStatus.BAD_REQUEST, "SEARCH_001", "검색어는 최대 100자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
