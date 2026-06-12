package com.test.ludence.recommendation.domain.entity;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.recommendation.domain.info.RecommendationErrorInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "recommendation_states")
public class RecommendationState {

    @Id
    private Long userId;

    @Column(nullable = false)
    private long requestedVersion;

    @Column(nullable = false)
    private long calculatedVersion;

    protected RecommendationState() {
    }

    private RecommendationState(Long userId) {
        this.userId = userId;
        this.requestedVersion = 1;
    }

    public static RecommendationState create(Long userId) {
        validateId(userId);
        return new RecommendationState(userId);
    }

    public void requestRefresh() {
        requestedVersion++;
    }

    public void completeCalculation(long version) {
        if (version < 1 || version > requestedVersion || version < calculatedVersion) {
            throw new DomainException(RecommendationErrorInfo.INVALID_VERSION);
        }
        calculatedVersion = version;
    }

    public boolean requiresCalculation() {
        return requestedVersion > calculatedVersion;
    }

    private static void validateId(Long id) {
        if (id == null || id < 1) {
            throw new DomainException(RecommendationErrorInfo.INVALID_REFERENCE_ID);
        }
    }
}
