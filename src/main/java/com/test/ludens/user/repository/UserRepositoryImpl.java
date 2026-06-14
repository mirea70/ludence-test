package com.test.ludens.user.repository;

import static com.test.ludens.post.domain.entity.QPost.post;
import static com.test.ludens.user.domain.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.dto.response.UserDetailResponse;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<User> findActiveByIdForUpdate(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(user)
                .where(
                        user.id.eq(id),
                        user.deletedAt.isNull()
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }

    @Override
    public Optional<UserDetailResponse> findActiveDetailByUsername(String username) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(
                        UserDetailResponse.class,
                        user.username.value,
                        post.id.count(),
                        user.createdAt
                ))
                .from(user)
                .leftJoin(post).on(
                        post.authorId.eq(user.id),
                        post.deletedAt.isNull()
                )
                .where(
                        user.username.value.eq(username),
                        user.deletedAt.isNull()
                )
                .groupBy(user.id, user.username.value, user.createdAt)
                .fetchOne());
    }

    @Override
    public Optional<Long> findActiveIdByUsername(String username) {
        return Optional.ofNullable(queryFactory
                .select(user.id)
                .from(user)
                .where(
                        user.username.value.eq(username),
                        user.deletedAt.isNull()
                )
                .fetchOne());
    }

    @Override
    public List<UserDetailResponse> findAllActiveDetails() {
        return queryFactory
                .select(Projections.constructor(
                        UserDetailResponse.class,
                        user.username.value,
                        post.id.count(),
                        user.createdAt
                ))
                .from(user)
                .leftJoin(post).on(
                        post.authorId.eq(user.id),
                        post.deletedAt.isNull()
                )
                .where(user.deletedAt.isNull())
                .groupBy(user.id, user.username.value, user.createdAt)
                .orderBy(user.createdAt.desc(), user.id.desc())
                .fetch();
    }
}
