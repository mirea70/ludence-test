package com.test.ludens.common.error.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorInfo implements ErrorInfo {

    INVALID_REFERENCE_ID(HttpStatus.BAD_REQUEST, "RECOMMENDATION_001", "추천 참조 ID가 유효하지 않습니다."),
    INVALID_RANK(HttpStatus.BAD_REQUEST, "RECOMMENDATION_002", "추천 후보 순위가 유효하지 않습니다."),
    INVALID_TIME(HttpStatus.BAD_REQUEST, "RECOMMENDATION_003", "추천 기준 시각이 유효하지 않습니다."),
    INVALID_VERSION(HttpStatus.BAD_REQUEST, "RECOMMENDATION_004", "추천 갱신 버전이 유효하지 않습니다."),
    INVALID_KEYWORD(HttpStatus.BAD_REQUEST, "RECOMMENDATION_005", "추천 검색 키워드가 유효하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
