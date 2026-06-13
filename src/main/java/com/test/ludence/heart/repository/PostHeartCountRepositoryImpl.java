package com.test.ludence.heart.repository;

import static com.test.ludence.heart.domain.entity.QPostHeartCount.postHeartCount;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostHeartCountRepositoryImpl implements PostHeartCountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long increase(Long postId) {
        return queryFactory
                .update(postHeartCount)
                .set(postHeartCount.count, postHeartCount.count.add(1))
                .where(postHeartCount.postId.eq(postId))
                .execute();
    }

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

    @Override
    public Optional<PostHeartCount> findByIdForUpdate(Long postId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(postHeartCount)
                .where(postHeartCount.postId.eq(postId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }
}
