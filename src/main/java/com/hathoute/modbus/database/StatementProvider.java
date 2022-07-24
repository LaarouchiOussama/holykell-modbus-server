package com.hathoute.modbus.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public interface StatementProvider {

    PreparedStatement create(Connection connection) throws SQLException;

    static StatementProvider raw(String sqlQuery) {
        return connection -> connection.prepareStatement(sqlQuery);
    }

}
