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
}
