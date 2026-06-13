package com.test.ludence.user.repository;

import static com.test.ludence.user.domain.entity.QUserSearchKeyword.userSearchKeyword;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.test.ludence.common.config.DatabaseProduct;
import com.test.ludence.user.domain.entity.UserSearchKeyword;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
import jakarta.persistence.LockModeType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class UserSearchKeywordRepositoryImpl implements UserSearchKeywordRepositoryCustom {

    private static final String UPSERT_SQL = """
            INSERT INTO user_search_keywords (user_id, keyword, search_count, last_searched_at)
            VALUES (?, ?, 1, ?)
            ON CONFLICT (user_id, keyword)
            DO UPDATE SET
                search_count = user_search_keywords.search_count + 1,
                last_searched_at = GREATEST(user_search_keywords.last_searched_at, EXCLUDED.last_searched_at)
            """;
    private static final String MERGE_SQL = """
            MERGE INTO user_search_keywords target
            USING (VALUES (?, ?, 1, ?)) source (user_id, keyword, search_count, last_searched_at)
            ON (target.user_id = source.user_id AND target.keyword = source.keyword)
            WHEN MATCHED THEN UPDATE SET
                target.search_count = target.search_count + 1,
                target.last_searched_at = GREATEST(target.last_searched_at, source.last_searched_at)
            WHEN NOT MATCHED THEN INSERT (user_id, keyword, search_count, last_searched_at)
                VALUES (source.user_id, source.keyword, source.search_count, source.last_searched_at)
            """;

    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseProduct databaseProduct;

    @Override
    public void upsert(Long userId, String keyword, Instant searchedAt) {
        jdbcTemplate.update(upsertSql(), userId, keyword, Timestamp.from(searchedAt));
    }

    private String upsertSql() {
        return databaseProduct.isPostgresql() ? UPSERT_SQL : MERGE_SQL;
    }

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
}
