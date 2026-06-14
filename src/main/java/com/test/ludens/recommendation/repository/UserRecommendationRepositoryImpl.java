package com.test.ludens.recommendation.repository;

import static com.test.ludens.recommendation.domain.entity.QUserRecommendation.userRecommendation;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRecommendationRepositoryImpl implements UserRecommendationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteByUserId(Long userId) {
        return queryFactory
                .delete(userRecommendation)
                .where(userRecommendation.id.userId.eq(userId))
                .execute();
    }
}
