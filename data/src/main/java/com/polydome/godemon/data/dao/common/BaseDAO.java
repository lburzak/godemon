package com.polydome.godemon.data.dao.common;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BaseDAO {
    private final DataSource dataSource;

    protected BaseDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    protected void withStatement(String sql, StatementConsumer onStatement) throws SQLException {
        try (final var connection = getConnection(); final var statement = connection.prepareStatement(sql)) {
            onStatement.accept(statement);
        }
    }
}
