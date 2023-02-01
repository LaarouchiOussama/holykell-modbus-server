package com.hathoute.modbus.database;

import com.hathoute.modbus.config.ConfigManager;
import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    @Getter
    private final DataSource dataSource;

    public DatabaseManager() {
        this(ConfigManager.getInstance().getProperty("database.user"),
                ConfigManager.getInstance().getIntProperty("database.port"),
                ConfigManager.getInstance().getProperty("database.password"),
                ConfigManager.getInstance().getProperty("database.hostname"));
    }

    public DatabaseManager(String user, int port, String password, String serverName) {
        logger.debug("MysqlDataSource: " + user + "@" + serverName + ":" + port);

        MysqlDataSource source = new MysqlDataSource();
        source.setUser(user);
        source.setPassword(password);
        source.setServerName(serverName);
        source.setPort(port);
        dataSource = source;
    }

    public void query(StatementProvider provider) throws SQLException {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = provider.create(connection)) {

            statement.execute();
        }
    }

    public <T> T query(StatementProvider provider, ResultParser<T> parser) throws SQLException {
        try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = provider.create(connection);
                ResultSet rs = statement.executeQuery()) {

            return parser.parse(rs);
        }
    }

    public void tryInitialize() throws SQLException {
        String databaseName = ConfigManager.getInstance()
                .getProperty("database.database_name");

        String database = "CREATE DATABASE IF NOT EXISTS " + databaseName + ";";
        query(StatementProvider.raw(database));

        ((MysqlDataSource) dataSource).setDatabaseName(databaseName);

        String devicesTable = "CREATE TABLE IF NOT EXISTS `devices`  (\n" +
                    "id varchar(255) PRIMARY KEY,\n"+
                    "name varchar(255),\n"+
                    "asset_id varchar(255),\n"+
                    "parent_device varchar(255),\n"+
                    "is_gateway boolean DEFAULT false,\n"+
                    "model varchar(255),\n"+
                    "protocol varchar(255),\n"+
                    "token varchar(255),\n"+
                    "device_sn varchar(255),\n"+
                    "serial_id varchar(255),\n"+
                    "slave_id varchar(255),\n"+
                    "created timestamp DEFAULT NOW(),\n"+
                    "updated timestamp DEFAULT NOW(),\n"+
                    "FOREIGN KEY (asset_id) REFERENCES assets(id),\n"+
                    "FOREIGN KEY (parent_device) REFERENCES devices(id) ON DELETE CASCADE\n"+
                ");";
        query(StatementProvider.raw(devicesTable));

        String metricsTable = "CREATE TABLE IF NOT EXISTS `metrics`  (\n" +
                    "id varchar(255) PRIMARY KEY,\n"+
                    "device_id varchar(255),\n"+
                    "name varchar(255),\n"+
                    "unit varchar(255),\n"+
                    "value_type varchar(255),\n"+
                    "refresh_rate int,\n"+
                    "decimal_places int,\n"+
                    "byte_order varchar(255),\n"+
                    "register_start varchar(255),\n"+
                    "slave_id varchar(255),\n"+
                    "function_code varchar(255),\n"+
                    "data_format varchar(255),\n"+
                    "created timestamp DEFAULT NOW(),\n"+
                    "updated timestamp DEFAULT NOW(),\n"+
                    "FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE\n"+
                ");";
        query(StatementProvider.raw(metricsTable));

        String metricsDataTable = "CREATE TABLE IF NOT EXISTS `metrics_data`  (\n" +
                "  `id` int NOT NULL AUTO_INCREMENT,\n" +
                "  `metric_id` varchar(255) NOT NULL,\n" +
                "  `value` double NOT NULL,\n" +
                "  `timestamp` timestamp NOT NULL,\n" +
                "   CONSTRAINT fk_metrics_data__metrics\n" +
                "        FOREIGN KEY (metric_id) REFERENCES metrics (id)\n" +
                "        ON DELETE CASCADE," +
                "  PRIMARY KEY (`id`)\n" +
                ");";
        query(StatementProvider.raw(metricsDataTable));
    }
}
