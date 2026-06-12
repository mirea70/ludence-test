package com.test.ludence.recommendation.repository;

import com.test.ludence.recommendation.domain.entity.CommonRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonRecommendationRepository extends JpaRepository<CommonRecommendation, Long> {
}
