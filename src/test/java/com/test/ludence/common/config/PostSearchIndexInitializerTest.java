package com.test.ludence.common.config;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("PostSearchIndexInitializer 테스트")
class PostSearchIndexInitializerTest {

    @Test
    @DisplayName("pg_trgm 확장 생성 후 활성 게시글 제목과 설명에 trigram 인덱스를 생성한다")
    void createsTrigramIndexesForActivePostSearch() {
        // given
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ApplicationArguments arguments = mock(ApplicationArguments.class);
        PostSearchIndexInitializer initializer = new PostSearchIndexInitializer(jdbcTemplate);

        // when
        initializer.run(arguments);

        // then
        InOrder inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
        inOrder.verify(jdbcTemplate).execute("""
                CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_posts_title_trgm_active
                ON posts USING GIN (lower(title) gin_trgm_ops)
                WHERE deleted_at IS NULL
                """);
        inOrder.verify(jdbcTemplate).execute("""
                CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_posts_description_trgm_active
                ON posts USING GIN (lower(description) gin_trgm_ops)
                WHERE deleted_at IS NULL
                """);
    }
}
