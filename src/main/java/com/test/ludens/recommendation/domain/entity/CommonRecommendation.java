package com.test.ludens.recommendation.domain.entity;

import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.common.error.info.RecommendationErrorInfo;
import com.test.ludens.recommendation.domain.vo.RecommendationRank;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "common_recommendations",
        indexes = @Index(name = "idx_common_recommendations_rank", columnList = "rank")
)
public class CommonRecommendation {

    @Id
    private Long postId;

    @Embedded
    private RecommendationRank rank;

    @Column(nullable = false)
    private Instant calculatedAt;

    protected CommonRecommendation() {
    }

    private CommonRecommendation(Long postId, RecommendationRank rank, Instant calculatedAt) {
        this.postId = postId;
        this.rank = rank;
        this.calculatedAt = calculatedAt;
    }

    public static CommonRecommendation create(Long postId, int rank, Instant calculatedAt) {
        validateId(postId);
        validateTime(calculatedAt);
        return new CommonRecommendation(postId, new RecommendationRank(rank), calculatedAt);
    }

    public int getRank() {
        return rank.value();
    }

    private static void validateId(Long id) {
        if (id == null || id < 1) {
            throw new DomainException(RecommendationErrorInfo.INVALID_REFERENCE_ID);
        }
    }

    private static void validateTime(Instant time) {
        if (time == null) {
            throw new DomainException(RecommendationErrorInfo.INVALID_TIME);
        }
    }
}
