package com.test.ludence.recommendation.repository;

import static com.test.ludence.recommendation.domain.entity.QRecommendationState.recommendationState;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.recommendation.domain.entity.RecommendationState;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecommendationStateRepositoryImpl implements RecommendationStateRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long incrementRequestedVersion(Long userId) {
        return queryFactory
                .update(recommendationState)
                .set(
                        recommendationState.requestedVersion,
                        recommendationState.requestedVersion.add(1)
                )
                .where(recommendationState.userId.eq(userId))
                .execute();
    }

    @Override
    public List<Long> findPendingUserIds(int limit) {
        return queryFactory
                .select(recommendationState.userId)
                .from(recommendationState)
                .where(recommendationState.requestedVersion.gt(recommendationState.calculatedVersion))
                .orderBy(recommendationState.userId.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<RecommendationState> findByUserIdForUpdate(Long userId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(recommendationState)
                .where(recommendationState.userId.eq(userId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }
}
