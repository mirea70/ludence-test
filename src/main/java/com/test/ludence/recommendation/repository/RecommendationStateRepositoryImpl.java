package com.test.ludence.recommendation.repository;

import static com.test.ludence.recommendation.domain.entity.QRecommendationState.recommendationState;
import static com.test.ludence.heart.domain.entity.QHeart.heart;
import static com.test.ludence.user.domain.entity.QUserPostView.userPostView;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.post.domain.entity.QPost;
import com.test.ludence.recommendation.domain.entity.RecommendationState;
import jakarta.persistence.LockModeType;
import java.time.Instant;
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
    public List<Long> findUserIdsAffectedByHeartedAuthors(int limit) {
        QPost sourcePost = new QPost("sourcePost");
        QPost newPost = new QPost("newPost");
        return queryFactory
                .select(recommendationState.userId)
                .distinct()
                .from(recommendationState)
                .join(heart).on(heart.id.userId.eq(recommendationState.userId))
                .join(sourcePost).on(
                        sourcePost.id.eq(heart.id.postId),
                        sourcePost.authorId.isNotNull(),
                        sourcePost.authorId.ne(recommendationState.userId),
                        sourcePost.deletedAt.isNull()
                )
                .join(newPost).on(
                        newPost.authorId.eq(sourcePost.authorId),
                        newPost.createdAt.gt(recommendationState.lastCalculatedAt),
                        newPost.deletedAt.isNull()
                )
                .where(recommendationState.lastCalculatedAt.isNotNull())
                .orderBy(recommendationState.userId.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Long> findUserIdsAffectedByViewedAuthors(Instant activitySince, int limit) {
        QPost sourcePost = new QPost("sourcePost");
        QPost newPost = new QPost("newPost");
        return queryFactory
                .select(recommendationState.userId)
                .distinct()
                .from(recommendationState)
                .join(userPostView).on(
                        userPostView.id.userId.eq(recommendationState.userId),
                        userPostView.lastViewedAt.goe(activitySince)
                )
                .join(sourcePost).on(
                        sourcePost.id.eq(userPostView.id.postId),
                        sourcePost.authorId.isNotNull(),
                        sourcePost.authorId.ne(recommendationState.userId),
                        sourcePost.deletedAt.isNull()
                )
                .join(newPost).on(
                        newPost.authorId.eq(sourcePost.authorId),
                        newPost.createdAt.gt(recommendationState.lastCalculatedAt),
                        newPost.deletedAt.isNull()
                )
                .where(recommendationState.lastCalculatedAt.isNotNull())
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
