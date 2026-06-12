package com.test.ludence.post.repository;

import static com.test.ludence.post.domain.entity.QPostViewCount.postViewCount;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.post.domain.entity.PostViewCount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostViewCountRepositoryImpl implements PostViewCountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<PostViewCount> findByIdForUpdate(Long postId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(postViewCount)
                .where(postViewCount.postId.eq(postId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }
}
