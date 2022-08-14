package com.hathoute.modbus.database.migration;

import com.hathoute.modbus.database.DatabaseManager;
import com.hathoute.modbus.database.StatementProvider;

import java.sql.SQLException;

public class Migration1 implements DatabaseMigration {

    @Override
    public String version() {
        return "1.1.0";
    }

    @Override
    public void execute(DatabaseManager databaseManager) throws SQLException {
        databaseManager.query(StatementProvider.raw("alter table metrics\n" +
                "    add unit varchar(255) not null;"));
    }
}
