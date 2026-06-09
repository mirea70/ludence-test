package com.test.ludence.user.repository;

import static com.test.ludence.user.domain.entity.QUser.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.user.domain.entity.User;
import jakarta.persistence.LockModeType;
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
}
