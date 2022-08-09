package com.hathoute.modbus.database;

import com.hathoute.modbus.config.ConfigManager;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private final DataSource dataSource;

    public DatabaseManager() {
        this(ConfigManager.getInstance().getProperty("database.user"),
                ConfigManager.getInstance().getProperty("database.password"),
                ConfigManager.getInstance().getProperty("database.hostname"));
    }

    public DatabaseManager(String user, String password, String serverName) {
        logger.debug("MysqlDataSource: " + user + "@" + serverName + " - " + password);

        MysqlDataSource source = new MysqlDataSource();
        source.setUser(user);
        source.setPassword(password);
        source.setServerName(serverName);
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
                "  `id` int(0) NOT NULL AUTO_INCREMENT,\n" +
                "  `serial_id` varchar(255) NOT NULL,\n" +
                "  `name` varchar(255) NOT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ");";
        query(StatementProvider.raw(devicesTable));

        String metricsTable = "CREATE TABLE IF NOT EXISTS `metrics`  (\n" +
                "  `id` int(0) NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(255) NOT NULL,\n" +
                "  `device_id` int(0) NOT NULL,\n" +
                "  `slave_id` int(0) NOT NULL,\n" +
                "  `function_code` tinyint(0) NOT NULL,\n" +
                "  `register_start` int(0) NOT NULL,\n" +
                "  `data_format` varchar(255) NOT NULL,\n" +
                "  `byte_order` varchar(255) NULL,\n" +
                "  `refresh_rate` int(0) NOT NULL,\n" +
                "  `unit` varchar(255) NOT NULL,\n" +
                "   CONSTRAINT fk_metrics__device\n" +
                "        FOREIGN KEY (device_id) REFERENCES devices (id)\n" +
                "        ON DELETE CASCADE," +
                "  PRIMARY KEY (`id`)\n" +
                ");";
        query(StatementProvider.raw(metricsTable));

        String metricsDataTable = "CREATE TABLE IF NOT EXISTS `metrics_data`  (\n" +
                "  `id` int(0) NOT NULL AUTO_INCREMENT,\n" +
                "  `metric_id` int(0) NOT NULL,\n" +
                "  `value` tinyblob NOT NULL,\n" +
                "  `timestamp` timestamp NOT NULL,\n" +
                "   CONSTRAINT fk_metrics_data__metrics\n" +
                "        FOREIGN KEY (metric_id) REFERENCES metrics (id)\n" +
                "        ON DELETE CASCADE," +
                "  PRIMARY KEY (`id`)\n" +
                ");";
        query(StatementProvider.raw(metricsDataTable));
    }
}
