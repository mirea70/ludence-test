package com.test.ludens.user.repository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.test.ludens.common.config.DatabaseProduct;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("사용자 활동 UPSERT 저장소 테스트")
class UserActivityUpsertRepositoryTest {

    private static final Instant RECORDED_AT = Instant.parse("2026-06-13T10:00:00Z");

    @Test
    @DisplayName("포스트 조회 활동을 PostgreSQL UPSERT로 저장한다")
    void upsertsPostView() {
        // given
        JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        DatabaseProduct databaseProduct = org.mockito.Mockito.mock(DatabaseProduct.class);
        when(databaseProduct.isPostgresql()).thenReturn(true);
        UserPostViewRepositoryImpl repository = new UserPostViewRepositoryImpl(null, jdbcTemplate, databaseProduct);

        // when
        repository.upsert(1L, 10L, RECORDED_AT);

        // then
        verify(jdbcTemplate).update(
                """
                INSERT INTO user_post_views (user_id, post_id, view_count, last_viewed_at)
                VALUES (?, ?, 1, ?)
                ON CONFLICT (user_id, post_id)
                DO UPDATE SET
                    view_count = user_post_views.view_count + 1,
                    last_viewed_at = GREATEST(user_post_views.last_viewed_at, EXCLUDED.last_viewed_at)
                """,
                1L,
                10L,
                Timestamp.from(RECORDED_AT)
        );
    }

    @Test
    @DisplayName("검색 활동을 PostgreSQL UPSERT로 저장한다")
    void upsertsSearchKeyword() {
        // given
        JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        DatabaseProduct databaseProduct = org.mockito.Mockito.mock(DatabaseProduct.class);
        when(databaseProduct.isPostgresql()).thenReturn(true);
        UserSearchKeywordRepositoryImpl repository = new UserSearchKeywordRepositoryImpl(null, jdbcTemplate, databaseProduct);

        // when
        repository.upsert(1L, "spring", RECORDED_AT);

        // then
        verify(jdbcTemplate).update(
                """
                INSERT INTO user_search_keywords (user_id, keyword, search_count, last_searched_at)
                VALUES (?, ?, 1, ?)
                ON CONFLICT (user_id, keyword)
                DO UPDATE SET
                    search_count = user_search_keywords.search_count + 1,
                    last_searched_at = GREATEST(user_search_keywords.last_searched_at, EXCLUDED.last_searched_at)
                """,
                1L,
                "spring",
                Timestamp.from(RECORDED_AT)
        );
    }
}
