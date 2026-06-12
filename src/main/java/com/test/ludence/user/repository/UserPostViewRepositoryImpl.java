package com.test.ludence.user.repository;

import static com.test.ludence.user.domain.entity.QUserPostView.userPostView;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.vo.UserPostViewId;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserPostViewRepositoryImpl implements UserPostViewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserPostView> findByUserIdAndPostIdForUpdate(Long userId, Long postId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(userPostView)
                .where(
                        userPostView.id.userId.eq(userId),
                        userPostView.id.postId.eq(postId)
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }

    @Override
    public List<UserPostViewId> findIdsLastViewedBefore(Instant expiredAt, int limit) {
        return queryFactory
                .select(Projections.constructor(
                        UserPostViewId.class,
                        userPostView.id.userId,
                        userPostView.id.postId
                ))
                .from(userPostView)
                .where(userPostView.lastViewedAt.lt(expiredAt))
                .orderBy(userPostView.lastViewedAt.asc(), userPostView.id.userId.asc(), userPostView.id.postId.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<UserPostViewId> findIdsExceedingLimitByUserId(Long userId, int maxCount) {
        return queryFactory
                .select(Projections.constructor(
                        UserPostViewId.class,
                        userPostView.id.userId,
                        userPostView.id.postId
                ))
                .from(userPostView)
                .where(userPostView.id.userId.eq(userId))
                .orderBy(userPostView.lastViewedAt.desc(), userPostView.id.postId.desc())
                .offset(maxCount)
                .fetch();
    }
}
