package com.test.ludence.user.repository;

import static com.test.ludence.user.domain.entity.QUserPostView.userPostView;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.common.config.DatabaseProduct;
import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.vo.UserPostViewId;
import jakarta.persistence.LockModeType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class UserPostViewRepositoryImpl implements UserPostViewRepositoryCustom {

    private static final String UPSERT_SQL = """
            INSERT INTO user_post_views (user_id, post_id, view_count, last_viewed_at)
            VALUES (?, ?, 1, ?)
            ON CONFLICT (user_id, post_id)
            DO UPDATE SET
                view_count = user_post_views.view_count + 1,
                last_viewed_at = GREATEST(user_post_views.last_viewed_at, EXCLUDED.last_viewed_at)
            """;

    private static final String MERGE_SQL = """
            MERGE INTO user_post_views target
            USING (VALUES (?, ?, 1, ?)) source (user_id, post_id, view_count, last_viewed_at)
            ON (target.user_id = source.user_id AND target.post_id = source.post_id)
            WHEN MATCHED THEN UPDATE SET
                target.view_count = target.view_count + 1,
                target.last_viewed_at = GREATEST(target.last_viewed_at, source.last_viewed_at)
            WHEN NOT MATCHED THEN INSERT (user_id, post_id, view_count, last_viewed_at)
                VALUES (source.user_id, source.post_id, source.view_count, source.last_viewed_at)
            """;

    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseProduct databaseProduct;

    @Override
    public void upsert(Long userId, Long postId, Instant viewedAt) {
        jdbcTemplate.update(upsertSql(), userId, postId, Timestamp.from(viewedAt));
    }

    private String upsertSql() {
        return databaseProduct.isPostgresql() ? UPSERT_SQL : MERGE_SQL;
    }

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

    @Override
    public List<UserPostViewId> findIdsLastViewedBefore(Instant expiredAt, int limit) {
        return queryFactory
                .select(Projections.constructor(
                        UserPostViewId.class,
                        userPostView.id.userId,
                        userPostView.id.postId
                ))
                .from(userPostView)
                .where(userPostView.lastViewedAt.lt(expiredAt))
                .orderBy(userPostView.lastViewedAt.asc(), userPostView.id.userId.asc(), userPostView.id.postId.asc())
                .limit(limit)
                .fetch();
    }
}
