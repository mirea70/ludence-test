package com.test.ludence.post.repository;

import static com.test.ludence.post.domain.entity.QPost.post;
import static com.test.ludence.heart.domain.entity.QHeart.heart;
import static com.test.ludence.heart.domain.entity.QPostHeartCount.postHeartCount;
import static com.test.ludence.user.domain.entity.QUser.user;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.common.page.PageRequest;
import jakarta.persistence.LockModeType;
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
}
