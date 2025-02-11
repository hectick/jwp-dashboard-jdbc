package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback {

    PreparedStatement prepareStatement(final Connection connection) throws SQLException;
}
