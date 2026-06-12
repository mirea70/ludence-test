package com.test.ludence.recommendation.repository;

import com.test.ludence.recommendation.domain.entity.RecommendationState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationStateRepository
        extends JpaRepository<RecommendationState, Long>, RecommendationStateRepositoryCustom {
}
