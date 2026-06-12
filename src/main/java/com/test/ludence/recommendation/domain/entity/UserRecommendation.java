package com.test.ludence.recommendation.domain.entity;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.recommendation.domain.info.RecommendationErrorInfo;
import com.test.ludence.recommendation.domain.vo.RecommendationRank;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "user_recommendations",
        indexes = @Index(name = "idx_user_recommendations_user_rank", columnList = "user_id, rank")
)
public class UserRecommendation {

    @EmbeddedId
    private UserRecommendationId id;

    @Embedded
    private RecommendationRank rank;

    @Column(nullable = false)
    private Instant calculatedAt;

    protected UserRecommendation() {
    }

    private UserRecommendation(UserRecommendationId id, RecommendationRank rank, Instant calculatedAt) {
        this.id = id;
        this.rank = rank;
        this.calculatedAt = calculatedAt;
    }

    public static UserRecommendation create(Long userId, Long postId, int rank, Instant calculatedAt) {
        validateId(userId);
        validateId(postId);
        validateTime(calculatedAt);
        return new UserRecommendation(
                new UserRecommendationId(userId, postId),
                new RecommendationRank(rank),
                calculatedAt
        );
    }

    public Long getUserId() {
        return id.userId();
    }

    public Long getPostId() {
        return id.postId();
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
