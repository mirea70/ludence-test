package com.test.ludens.post.repository;

import static com.test.ludens.post.domain.entity.QPost.post;
import static com.test.ludens.post.domain.entity.QPostViewCount.postViewCount;
import static com.test.ludens.heart.domain.entity.QHeart.heart;
import static com.test.ludens.heart.domain.entity.QPostHeartCount.postHeartCount;
import static com.test.ludens.recommendation.domain.entity.QCommonRecommendation.commonRecommendation;
import static com.test.ludens.recommendation.domain.entity.QUserRecommendation.userRecommendation;
import static com.test.ludens.user.domain.entity.QUser.user;
import static com.test.ludens.user.domain.entity.QUserPostView.userPostView;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.common.page.PageRequest;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<String> findActiveImageKeyById(Long postId) {
        return Optional.ofNullable(queryFactory
                .select(post.imageKey.value)
                .from(post)
                .where(
                        post.id.eq(postId),
                        post.deletedAt.isNull()
                )
                .fetchOne());
    }

    @Override
    public Optional<PostDetailResponse> findActiveDetailById(Long postId, Long currentUserId) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(
                        PostDetailResponse.class,
                        post.id,
                        post.title.value,
                        post.description.value,
                        post.createdAt,
                        post.editedAt,
                        user.username.value,
                        postHeartCount.count,
                        heartedExpression(currentUserId)
                ))
                .from(post)
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(user).on(
                        user.id.eq(post.authorId),
                        user.deletedAt.isNull()
                )
                .where(
                        post.id.eq(postId),
                        post.deletedAt.isNull()
                )
                .fetchOne());
    }

    @Override
    public List<PostDetailResponse> findActiveDetailsByAuthorId(
            Long authorId,
            String username,
            Long currentUserId,
            PageRequest pageRequest
    ) {
        return queryFactory
                .select(Projections.constructor(
                        PostDetailResponse.class,
                        post.id,
                        post.title.value,
                        post.description.value,
                        post.createdAt,
                        post.editedAt,
                        Expressions.constant(username),
                        postHeartCount.count,
                        heartedExpression(currentUserId)
                ))
                .from(post)
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .where(
                        post.authorId.eq(authorId),
                        post.deletedAt.isNull()
                )
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(pageRequest.offset())
                .limit(pageRequest.limit())
                .fetch();
    }

    @Override
    public long countActiveByAuthorId(Long authorId) {
        Long count = queryFactory
                .select(post.id.count())
                .from(post)
                .where(
                        post.authorId.eq(authorId),
                        post.deletedAt.isNull()
                )
                .fetchOne();
        return count == null ? 0L : count;
    }

    private Expression<Boolean> heartedExpression(Long currentUserId) {
        if (currentUserId == null) {
            return Expressions.FALSE;
        }
        return JPAExpressions.selectOne()
                .from(heart)
                .where(
                        heart.id.userId.eq(currentUserId),
                        heart.id.postId.eq(post.id)
                )
                .exists();
    }

    @Override
    public Optional<Post> findActiveByIdForUpdate(Long postId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(post)
                .where(
                        post.id.eq(postId),
                        post.deletedAt.isNull()
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }

    @Override
    public Optional<PostHeartAccess> findActiveHeartAccessById(Long postId) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(
                        PostHeartAccess.class,
                        post.authorId,
                        postHeartCount.count
                ))
                .from(post)
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .where(
                        post.id.eq(postId),
                        post.deletedAt.isNull()
                )
                .fetchOne());
    }

    @Override
    public List<PostDetailResponse> findActiveDetailsHeartedByUserId(Long userId, PageRequest pageRequest) {
        return queryFactory
                .select(Projections.constructor(
                        PostDetailResponse.class,
                        post.id,
                        post.title.value,
                        post.description.value,
                        post.createdAt,
                        post.editedAt,
                        user.username.value,
                        postHeartCount.count,
                        Expressions.TRUE
                ))
                .from(heart)
                .join(post).on(
                        post.id.eq(heart.id.postId),
                        post.deletedAt.isNull()
                )
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(user).on(
                        user.id.eq(post.authorId),
                        user.deletedAt.isNull()
                )
                .where(heart.id.userId.eq(userId))
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(pageRequest.offset())
                .limit(pageRequest.limit())
                .fetch();
    }

    @Override
    public long countActiveHeartedByUserId(Long userId) {
        Long count = queryFactory
                .select(post.id.count())
                .from(heart)
                .join(post).on(
                        post.id.eq(heart.id.postId),
                        post.deletedAt.isNull()
                )
                .where(heart.id.userId.eq(userId))
                .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public List<PostDetailResponse> findActiveDetailsByQuery(
            String query,
            Long currentUserId,
            PageRequest pageRequest
    ) {
        return queryFactory
                .select(Projections.constructor(
                        PostDetailResponse.class,
                        post.id,
                        post.title.value,
                        post.description.value,
                        post.createdAt,
                        post.editedAt,
                        user.username.value,
                        postHeartCount.count,
                        heartedExpression(currentUserId)
                ))
                .from(post)
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(user).on(
                        user.id.eq(post.authorId),
                        user.deletedAt.isNull()
                )
                .where(
                        post.deletedAt.isNull(),
                        containsQuery(query)
                )
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(pageRequest.offset())
                .limit(pageRequest.limit())
                .fetch();
    }

    @Override
    public long countActiveByQuery(String query) {
        Long count = queryFactory
                .select(post.id.count())
                .from(post)
                .where(
                        post.deletedAt.isNull(),
                        containsQuery(query)
                )
                .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public List<PostDetailResponse> findAllActiveDetails() {
        return queryFactory
                .select(Projections.constructor(
                        PostDetailResponse.class,
                        post.id,
                        post.title.value,
                        post.description.value,
                        post.createdAt,
                        post.editedAt,
                        user.username.value,
                        postHeartCount.count,
                        Expressions.FALSE
                ))
                .from(post)
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(user).on(
                        user.id.eq(post.authorId),
                        user.deletedAt.isNull()
                )
                .where(post.deletedAt.isNull())
                .orderBy(post.createdAt.desc(), post.id.desc())
                .fetch();
    }

    @Override
    public List<String> findAllImageKeys() {
        return queryFactory
                .select(post.imageKey.value)
                .from(post)
                .fetch();
    }

    @Override
    public List<PostCleanupCandidate> findCleanupCandidates(
            Instant expiredAt,
            Instant cursorDeletedAt,
            Long cursorPostId,
            int limit
    ) {
        return queryFactory
                .select(Projections.constructor(
                        PostCleanupCandidate.class,
                        post.id,
                        post.imageKey.value,
                        post.deletedAt
                ))
                .from(post)
                .where(
                        post.deletedAt.loe(expiredAt),
                        afterCleanupCursor(cursorDeletedAt, cursorPostId)
                )
                .orderBy(post.deletedAt.asc(), post.id.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public long deleteExpiredPostData(Long postId, Instant expiredAt) {
        Long lockedPostId = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        post.id.eq(postId),
                        post.deletedAt.loe(expiredAt)
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();
        if (lockedPostId == null) {
            return 0;
        }

        queryFactory.delete(heart).where(heart.id.postId.eq(postId)).execute();
        queryFactory.delete(userPostView).where(userPostView.id.postId.eq(postId)).execute();
        queryFactory.delete(userRecommendation).where(userRecommendation.id.postId.eq(postId)).execute();
        queryFactory.delete(commonRecommendation).where(commonRecommendation.postId.eq(postId)).execute();
        queryFactory.delete(postViewCount).where(postViewCount.postId.eq(postId)).execute();
        queryFactory.delete(postHeartCount).where(postHeartCount.postId.eq(postId)).execute();
        return queryFactory.delete(post).where(post.id.eq(postId)).execute();
    }

    private BooleanExpression afterCleanupCursor(Instant cursorDeletedAt, Long cursorPostId) {
        if (cursorDeletedAt == null || cursorPostId == null) {
            return null;
        }
        return post.deletedAt.gt(cursorDeletedAt)
                .or(post.deletedAt.eq(cursorDeletedAt).and(post.id.gt(cursorPostId)));
    }

    private BooleanExpression containsQuery(String query) {
        if (query == null) {
            return null;
        }
        return post.title.value.containsIgnoreCase(query)
                .or(post.description.value.containsIgnoreCase(query));
    }
}
