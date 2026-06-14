package com.test.ludens.recommendation.repository;

import static com.test.ludens.heart.domain.entity.QHeart.heart;
import static com.test.ludens.heart.domain.entity.QPostHeartCount.postHeartCount;
import static com.test.ludens.post.domain.entity.QPost.post;
import static com.test.ludens.recommendation.domain.entity.QCommonRecommendation.commonRecommendation;
import static com.test.ludens.recommendation.domain.entity.QUserRecommendation.userRecommendation;
import static com.test.ludens.user.domain.entity.QUser.user;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludens.post.dto.response.PostDetailResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecommendationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PostDetailResponse> findCommon(Long currentUserId, int limit) {
        return queryFactory
                .select(postDetailProjection(currentUserId))
                .from(commonRecommendation)
                .join(post).on(
                        post.id.eq(commonRecommendation.postId),
                        post.deletedAt.isNull()
                )
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(user).on(
                        user.id.eq(post.authorId),
                        user.deletedAt.isNull()
                )
                .orderBy(commonRecommendation.rank.value.asc())
                .limit(limit)
                .fetch();
    }

    public long countActiveCommon() {
        Long count = queryFactory
                .select(commonRecommendation.postId.count())
                .from(commonRecommendation)
                .join(post).on(
                        post.id.eq(commonRecommendation.postId),
                        post.deletedAt.isNull()
                )
                .fetchOne();
        return count == null ? 0L : count;
    }

    public List<PostDetailResponse> findByUserId(Long userId, int limit) {
        return queryFactory
                .select(postDetailProjection(userId))
                .from(userRecommendation)
                .join(post).on(
                        post.id.eq(userRecommendation.id.postId),
                        post.deletedAt.isNull()
                )
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(user).on(
                        user.id.eq(post.authorId),
                        user.deletedAt.isNull()
                )
                .where(userRecommendation.id.userId.eq(userId))
                .orderBy(userRecommendation.rank.value.asc())
                .limit(limit)
                .fetch();
    }

    public long countActiveByUserId(Long userId) {
        Long count = queryFactory
                .select(userRecommendation.id.postId.count())
                .from(userRecommendation)
                .join(post).on(
                        post.id.eq(userRecommendation.id.postId),
                        post.deletedAt.isNull()
                )
                .where(userRecommendation.id.userId.eq(userId))
                .fetchOne();
        return count == null ? 0L : count;
    }

    private Expression<PostDetailResponse> postDetailProjection(Long currentUserId) {
        return Projections.constructor(
                PostDetailResponse.class,
                post.id,
                post.title.value,
                post.description.value,
                post.createdAt,
                post.editedAt,
                user.username.value,
                postHeartCount.count,
                heartedExpression(currentUserId)
        );
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
