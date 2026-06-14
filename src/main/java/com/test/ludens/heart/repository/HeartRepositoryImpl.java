package com.test.ludens.heart.repository;

import static com.test.ludens.heart.domain.entity.QHeart.heart;
import static com.test.ludens.user.domain.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludens.common.page.PageRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HeartRepositoryImpl implements HeartRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HeartCountByPostId> getCountsByUserId(Long userId) {
        return queryFactory
                .select(Projections.constructor(
                        HeartCountByPostId.class,
                        heart.id.postId,
                        heart.count()
                ))
                .from(heart)
                .where(heart.id.userId.eq(userId))
                .groupBy(heart.id.postId)
                .fetch();
    }

    @Override
    public long deleteByUserId(Long userId) {
        return queryFactory
                .delete(heart)
                .where(heart.id.userId.eq(userId))
                .execute();
    }

    @Override
    public long deleteByPostId(Long postId) {
        return queryFactory
                .delete(heart)
                .where(heart.id.postId.eq(postId))
                .execute();
    }

    @Override
    public long deleteByUserIdAndPostId(Long userId, Long postId) {
        return queryFactory
                .delete(heart)
                .where(
                        heart.id.userId.eq(userId),
                        heart.id.postId.eq(postId)
                )
                .execute();
    }

    @Override
    public List<String> findActiveUsernamesByPostId(Long postId, PageRequest pageRequest) {
        return queryFactory
                .select(user.username.value)
                .from(heart)
                .join(user).on(
                        user.id.eq(heart.id.userId),
                        user.deletedAt.isNull()
                )
                .where(heart.id.postId.eq(postId))
                .orderBy(user.username.value.asc())
                .offset(pageRequest.offset())
                .limit(pageRequest.limit())
                .fetch();
    }
}
