package com.andrewgilmartin.common.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

/**
 * Executes the given SQL and returns the first generated key. For MySql, this
 * generated key is the int associated with the auto_increment column.
 */
public class InsertAndReturnGeneratedKeyConnectionCallback implements ConnectionCallback<Integer> {

    // configured
    private String sql;
    private Object[] parameters;

    public InsertAndReturnGeneratedKeyConnectionCallback(String sql, Object... parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    @Override
    public Integer doInConnection(Connection connection) throws SQLException, DataAccessException {
        // insert the record
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            statement.execute();
            // get the auto-increment value
            try (ResultSet results = statement.getGeneratedKeys()) {
                if (!results.next()) {
                    throw results.getWarnings();
                }
                return results.getInt(1);
            }
        }
    }
}

// END
