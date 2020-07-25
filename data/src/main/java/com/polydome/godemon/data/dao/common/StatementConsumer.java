package com.polydome.godemon.data.dao.common;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementConsumer {
    void accept(PreparedStatement preparedStatement) throws SQLException;
}
