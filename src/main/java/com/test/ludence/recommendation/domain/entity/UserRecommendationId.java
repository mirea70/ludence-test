package com.test.ludence.recommendation.domain.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record UserRecommendationId(Long userId, Long postId) implements Serializable {
}
