package com.test.ludens.recommendation.repository;

import com.test.ludens.recommendation.domain.entity.RecommendationState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationStateRepository
        extends JpaRepository<RecommendationState, Long>, RecommendationStateRepositoryCustom {
}
