package com.test.ludence.common.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class PostSearchIndexInitializer implements ApplicationRunner {

    private static final String CREATE_TRIGRAM_EXTENSION = "CREATE EXTENSION IF NOT EXISTS pg_trgm";
    private static final String CREATE_TITLE_INDEX = """
            CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_posts_title_trgm_active
            ON posts USING GIN (lower(title) gin_trgm_ops)
            WHERE deleted_at IS NULL
            """;
    private static final String CREATE_DESCRIPTION_INDEX = """
            CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_posts_description_trgm_active
            ON posts USING GIN (lower(description) gin_trgm_ops)
            WHERE deleted_at IS NULL
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(CREATE_TRIGRAM_EXTENSION);
        jdbcTemplate.execute(CREATE_TITLE_INDEX);
        jdbcTemplate.execute(CREATE_DESCRIPTION_INDEX);
    }
}
