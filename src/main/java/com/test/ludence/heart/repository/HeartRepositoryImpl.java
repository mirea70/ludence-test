package com.test.ludence.heart.repository;

import static com.test.ludence.heart.domain.entity.QHeart.heart;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HeartRepositoryImpl implements HeartRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HeartCountByPostId> getCountsByUserId(Long userId) {
        return queryFactory
                .select(Projections.constructor(
                        HeartCountByPostId.class,
                        heart.id.postId,
                        heart.count()
                ))
                .from(heart)
                .where(heart.id.userId.eq(userId))
                .groupBy(heart.id.postId)
                .fetch();
    }

    @Override
    public long deleteByUserId(Long userId) {
        return queryFactory
                .delete(heart)
                .where(heart.id.userId.eq(userId))
                .execute();
    }

    @Override
    public long deleteByPostId(Long postId) {
        return queryFactory
                .delete(heart)
                .where(heart.id.postId.eq(postId))
                .execute();
    }
}
