package com.test.ludence.recommendation.repository;

import static com.test.ludence.recommendation.domain.entity.QRecommendationState.recommendationState;

import com.querydsl.jpa.impl.JPAQueryFactory;
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

}
