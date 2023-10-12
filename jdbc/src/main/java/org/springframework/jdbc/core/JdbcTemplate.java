package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(connection -> prepareStatement(sql, connection, args), PreparedStatement::executeUpdate);
    }

    private <T> T execute(final PreparedStatementCallback preparedStatementCallback,
                          final ExecutionCallback<T> executionCallback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement pstmt = preparedStatementCallback.prepareStatement(connection)) {
            return executionCallback.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private PreparedStatement prepareStatement(final String sql, final Connection connection, final Object[] args)
            throws SQLException {
        final PreparedStatement pstmt = connection.prepareStatement(sql);
        setParameters(pstmt, args);
        return pstmt;
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = queryForList(sql, rowMapper, args);
        if (results.isEmpty()) {
            throw new DataAccessException();
        }
        if (results.size() > 1) {
            throw new DataAccessException();
        }
        return results.iterator().next();
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(connection -> prepareStatement(sql, connection, args), pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }
}
