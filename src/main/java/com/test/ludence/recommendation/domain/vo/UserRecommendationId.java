package com.test.ludence.recommendation.domain.vo;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record UserRecommendationId(Long userId, Long postId) implements Serializable {
}
