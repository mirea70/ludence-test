package com.test.ludence.user.repository;

import static com.test.ludence.user.domain.entity.QUserSearchKeyword.userSearchKeyword;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.user.domain.entity.UserSearchKeyword;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserSearchKeywordRepositoryImpl implements UserSearchKeywordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserSearchKeyword> findByUserIdAndKeywordForUpdate(Long userId, String keyword) {
        return Optional.ofNullable(queryFactory
                .selectFrom(userSearchKeyword)
                .where(
                        userSearchKeyword.id.userId.eq(userId),
                        userSearchKeyword.id.keyword.eq(keyword)
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }
}
