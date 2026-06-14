package com.test.ludens.recommendation.repository;

import com.test.ludens.recommendation.domain.entity.UserRecommendation;
import com.test.ludens.recommendation.domain.vo.UserRecommendationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRecommendationRepository
        extends JpaRepository<UserRecommendation, UserRecommendationId>, UserRecommendationRepositoryCustom {
}
