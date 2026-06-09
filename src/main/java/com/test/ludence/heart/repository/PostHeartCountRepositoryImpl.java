package com.test.ludence.heart.repository;

import static com.test.ludence.heart.domain.entity.QPostHeartCount.postHeartCount;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostHeartCountRepositoryImpl implements PostHeartCountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long decrease(Long postId, long amount) {
        return queryFactory
                .update(postHeartCount)
                .set(postHeartCount.count, postHeartCount.count.subtract(amount))
                .where(
                        postHeartCount.postId.eq(postId),
                        postHeartCount.count.goe(amount)
                )
                .execute();
    }
}
