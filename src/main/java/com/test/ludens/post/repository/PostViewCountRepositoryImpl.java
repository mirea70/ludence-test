package com.test.ludens.post.repository;

import static com.test.ludens.post.domain.entity.QPostViewCount.postViewCount;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostViewCountRepositoryImpl implements PostViewCountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long increment(Long postId) {
        return queryFactory
                .update(postViewCount)
                .set(postViewCount.count, postViewCount.count.add(1))
                .where(postViewCount.postId.eq(postId))
                .execute();
    }
}
