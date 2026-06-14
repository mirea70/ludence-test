package com.test.ludens.recommendation.repository;

import static com.test.ludens.heart.domain.entity.QHeart.heart;
import static com.test.ludens.heart.domain.entity.QPostHeartCount.postHeartCount;
import static com.test.ludens.post.domain.entity.QPost.post;
import static com.test.ludens.post.domain.entity.QPostViewCount.postViewCount;
import static com.test.ludens.user.domain.entity.QUserPostView.userPostView;
import static com.test.ludens.user.domain.entity.QUserSearchKeyword.userSearchKeyword;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludens.heart.domain.entity.QHeart;
import com.test.ludens.heart.domain.entity.QPostHeartCount;
import com.test.ludens.post.domain.entity.QPost;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecommendationCandidateQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Long> findCommon(int limit) {
        return baseCandidateQuery()
                .orderBy(
                        postHeartCount.count.desc(),
                        postViewCount.count.coalesce(0L).desc(),
                        post.createdAt.desc(),
                        post.id.desc()
                )
                .limit(limit)
                .fetch();
    }

    public List<Long> findByHeartedAuthors(Long userId, int limit) {
        QHeart sourceHeart = new QHeart("sourceHeart");
        QPost sourcePost = new QPost("sourcePost");
        QPostHeartCount sourceHeartCount = new QPostHeartCount("sourceHeartCount");
        return queryFactory
                .select(post.id)
                .from(sourceHeart)
                .join(sourcePost).on(sourcePost.id.eq(sourceHeart.id.postId))
                .join(sourceHeartCount).on(sourceHeartCount.postId.eq(sourcePost.id))
                .join(post).on(post.authorId.eq(sourcePost.authorId))
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(postViewCount).on(postViewCount.postId.eq(post.id))
                .where(
                        sourceHeart.id.userId.eq(userId),
                        sourcePost.deletedAt.isNull(),
                        post.authorId.isNotNull(),
                        post.authorId.ne(userId),
                        post.deletedAt.isNull(),
                        notHeartedBy(userId)
                )
                .groupBy(post.id, postHeartCount.count, postViewCount.count, post.createdAt)
                .orderBy(sourceHeartCount.count.max().desc())
                .orderBy(candidateOrder())
                .limit(limit)
                .fetch();
    }

    public List<Long> findByViewedAuthors(Long userId, Instant since, int limit) {
        QPost sourcePost = new QPost("sourcePost");
        return queryFactory
                .select(post.id)
                .from(userPostView)
                .join(sourcePost).on(sourcePost.id.eq(userPostView.id.postId))
                .join(post).on(post.authorId.eq(sourcePost.authorId))
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(postViewCount).on(postViewCount.postId.eq(post.id))
                .where(
                        userPostView.id.userId.eq(userId),
                        userPostView.lastViewedAt.goe(since),
                        sourcePost.deletedAt.isNull(),
                        post.authorId.isNotNull(),
                        post.authorId.ne(userId),
                        post.deletedAt.isNull(),
                        notHeartedBy(userId)
                )
                .groupBy(post.id, postHeartCount.count, postViewCount.count, post.createdAt)
                .orderBy(userPostView.lastViewedAt.max().desc())
                .orderBy(candidateOrder())
                .limit(limit)
                .fetch();
    }

    public List<Long> findByRecentSearches(Long userId, Instant since, int limit) {
        BooleanExpression containsKeyword = Expressions.booleanTemplate(
                "lower({0}) like concat('%', lower({1}), '%') or lower(coalesce({2}, '')) like concat('%', lower({1}), '%')",
                post.title.value,
                userSearchKeyword.id.keyword,
                post.description.value
        );
        return baseCandidateQuery()
                .join(userSearchKeyword).on(
                        userSearchKeyword.id.userId.eq(userId),
                        userSearchKeyword.lastSearchedAt.goe(since),
                        containsKeyword
                )
                .where(
                        post.authorId.ne(userId).or(post.authorId.isNull()),
                        notHeartedBy(userId)
                )
                .orderBy(searchCandidateOrder())
                .limit(limit)
                .fetch();
    }

    public List<Long> findLatest(Long userId, int limit) {
        return baseCandidateQuery()
                .where(
                        post.authorId.ne(userId).or(post.authorId.isNull()),
                        notHeartedBy(userId)
                )
                .orderBy(post.createdAt.desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }

    private JPAQuery<Long> baseCandidateQuery() {
        return queryFactory
                .select(post.id)
                .from(post)
                .join(postHeartCount).on(postHeartCount.postId.eq(post.id))
                .leftJoin(postViewCount).on(postViewCount.postId.eq(post.id))
                .where(post.deletedAt.isNull());
    }

    private BooleanExpression notHeartedBy(Long userId) {
        return JPAExpressions.selectOne()
                .from(heart)
                .where(
                        heart.id.userId.eq(userId),
                        heart.id.postId.eq(post.id)
                )
                .notExists();
    }

    private OrderSpecifier<?>[] candidateOrder() {
        return new OrderSpecifier[]{
                postHeartCount.count.desc(),
                postViewCount.count.coalesce(0L).desc(),
                post.createdAt.desc(),
                post.id.desc()
        };
    }

    private OrderSpecifier<?>[] searchCandidateOrder() {
        return new OrderSpecifier[]{
                userSearchKeyword.lastSearchedAt.desc(),
                postHeartCount.count.desc(),
                postViewCount.count.coalesce(0L).desc(),
                post.createdAt.desc(),
                post.id.desc()
        };
    }
}
