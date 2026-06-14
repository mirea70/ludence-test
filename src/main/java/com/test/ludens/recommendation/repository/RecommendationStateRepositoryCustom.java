package com.test.ludens.recommendation.repository;

import com.test.ludens.recommendation.domain.entity.RecommendationState;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RecommendationStateRepositoryCustom {

    long incrementRequestedVersion(Long userId);

    List<Long> findPendingUserIds(int limit);

    List<Long> findUserIdsAffectedByHeartedAuthors(int limit);

    List<Long> findUserIdsAffectedByViewedAuthors(Instant activitySince, int limit);

    Optional<RecommendationState> findByUserIdForUpdate(Long userId);
}
