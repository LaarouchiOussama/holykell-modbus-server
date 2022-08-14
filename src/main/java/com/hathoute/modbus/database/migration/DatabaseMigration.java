package com.hathoute.modbus.database.migration;

import com.hathoute.modbus.database.DatabaseManager;

import java.sql.SQLException;

public interface DatabaseMigration {

    String version();

    void execute(DatabaseManager databaseManager) throws SQLException;

}
