package com.test.ludence.recommendation.repository;

import com.test.ludence.recommendation.domain.entity.UserRecommendation;
import com.test.ludence.recommendation.domain.entity.UserRecommendationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRecommendationRepository
        extends JpaRepository<UserRecommendation, UserRecommendationId>, UserRecommendationRepositoryCustom {
}
