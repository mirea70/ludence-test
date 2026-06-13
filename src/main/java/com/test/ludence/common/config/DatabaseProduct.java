package com.test.ludence.common.config;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class DatabaseProduct {

    private final boolean postgresql;

    public DatabaseProduct(DataSource dataSource) {
        try (var connection = dataSource.getConnection()) {
            postgresql = "PostgreSQL".equals(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new IllegalStateException("데이터베이스 제품 정보를 확인할 수 없습니다.", e);
        }
    }

    public boolean isPostgresql() {
        return postgresql;
    }
}
