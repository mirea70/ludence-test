package com.test.ludence.user.repository;

import static com.test.ludence.user.domain.entity.QUserPostView.userPostView;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.user.domain.entity.UserPostView;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserPostViewRepositoryImpl implements UserPostViewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<UserPostView> findByUserIdAndPostIdForUpdate(Long userId, Long postId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(userPostView)
                .where(
                        userPostView.id.userId.eq(userId),
                        userPostView.id.postId.eq(postId)
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }
}
