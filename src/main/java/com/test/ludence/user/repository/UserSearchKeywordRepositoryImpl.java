package com.test.ludence.user.repository;

import static com.test.ludence.user.domain.entity.QUserSearchKeyword.userSearchKeyword;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.user.domain.entity.UserSearchKeyword;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
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

    @Override
    public List<UserSearchKeywordId> findIdsLastSearchedBefore(Instant expiredAt, int limit) {
        return queryFactory
                .select(Projections.constructor(
                        UserSearchKeywordId.class,
                        userSearchKeyword.id.userId,
                        userSearchKeyword.id.keyword
                ))
                .from(userSearchKeyword)
                .where(userSearchKeyword.lastSearchedAt.lt(expiredAt))
                .orderBy(
                        userSearchKeyword.lastSearchedAt.asc(),
                        userSearchKeyword.id.userId.asc(),
                        userSearchKeyword.id.keyword.asc()
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public List<UserSearchKeywordId> findIdsExceedingLimitByUserId(Long userId, int maxCount) {
        return queryFactory
                .select(Projections.constructor(
                        UserSearchKeywordId.class,
                        userSearchKeyword.id.userId,
                        userSearchKeyword.id.keyword
                ))
                .from(userSearchKeyword)
                .where(userSearchKeyword.id.userId.eq(userId))
                .orderBy(userSearchKeyword.lastSearchedAt.desc(), userSearchKeyword.id.keyword.desc())
                .offset(maxCount)
                .fetch();
    }
}
