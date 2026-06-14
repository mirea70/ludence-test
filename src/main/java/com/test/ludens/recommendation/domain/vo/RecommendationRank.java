package com.test.ludens.recommendation.domain.vo;

import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.common.error.info.RecommendationErrorInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record RecommendationRank(
        @Column(name = "rank", nullable = false)
        int value
) {

    public static final int MAX_CANDIDATE_COUNT = 40;

    public RecommendationRank {
        if (value < 1 || value > MAX_CANDIDATE_COUNT) {
            throw new DomainException(RecommendationErrorInfo.INVALID_RANK);
        }
    }
}
