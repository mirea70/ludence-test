package com.test.ludence.post.repository;

import static com.test.ludence.post.domain.entity.QPost.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long clearAuthorId(Long authorId) {
        return queryFactory
                .update(post)
                .setNull(post.authorId)
                .where(post.authorId.eq(authorId))
                .execute();
    }
}
