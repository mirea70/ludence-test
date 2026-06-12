package com.test.ludence.recommendation.repository;

import com.test.ludence.recommendation.domain.entity.RecommendationState;
import java.util.List;
import java.util.Optional;

public interface RecommendationStateRepositoryCustom {

    long incrementRequestedVersion(Long userId);

    List<Long> findPendingUserIds(int limit);

    Optional<RecommendationState> findByUserIdForUpdate(Long userId);
}
