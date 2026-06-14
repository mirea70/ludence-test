package com.test.ludens.recommendation.repository;

import com.test.ludens.recommendation.domain.entity.CommonRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonRecommendationRepository extends JpaRepository<CommonRecommendation, Long> {
}
