package com.hathoute.modbus.database.migration;

import com.hathoute.modbus.database.DatabaseManager;
import com.hathoute.modbus.database.ResultParser;
import com.hathoute.modbus.database.StatementProvider;
import com.hathoute.modbus.exception.ByteLengthMismatchException;
import com.hathoute.modbus.parser.AbstractParser;
import com.hathoute.modbus.parser.Parser;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class Migration2 implements DatabaseMigration {

    protected static final Logger logger = LoggerFactory.getLogger(Migration2.class);

    @Override
    public String version() {
        return "2.0.0";
    }

    @Override
    public void execute(DatabaseManager databaseManager) throws SQLException {
        databaseManager.query(StatementProvider.raw("alter table metrics_data add rvalue double not null;"));

        List<Metric_v1_1> metrics = databaseManager.query(StatementProvider.raw("SELECT * FROM metrics"),
                ResultParser.listParser(Metric_v1_1.parser()));

        Connection connection = databaseManager.getDataSource().getConnection();

        for (Metric_v1_1 metric : metrics) {
            logger.info("Formatting metric data for metric " + metric.getName());
            AbstractParser parser = AbstractParser.getParser(metric.dataFormat, metric.byteOrder);

            List<MetricData_v1_1> data = databaseManager.query(MetricData_v1_1.selectDataByMetricProvider(metric.id),
                    ResultParser.listParser(MetricData_v1_1.parser()));
            for (MetricData_v1_1 md : data) {
                double formatted = 0;

                try {
                    formatted = parser.parse(md.value);
                } catch (ByteLengthMismatchException e) {
                    // Corrupted metric data, delete it...
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM metrics_data WHERE id = ?"
                    );
                    statement.setInt(1, md.id);
                    statement.execute();
                }

                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE metrics_data SET rvalue = ?, timestamp = timestamp WHERE id = ?"
                );
                statement.setDouble(1, formatted);
                statement.setInt(2, md.id);

                statement.execute();
                statement.closeOnCompletion();
            }
        }

        databaseManager.query(StatementProvider.raw("alter table metrics_data drop column value;"));
        databaseManager.query(StatementProvider.raw("alter table metrics_data change rvalue value double not null;"));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    static class Metric_v1_1 {

        private String id;
        @NonNull
        private String name;
        private String deviceId;
        private int slaveId;
        private byte functionCode;
        private int registerStart;
        @NonNull
        private String dataFormat;
        private String byteOrder;
        private int refreshRate;

        public static ResultParser<Metric_v1_1> parser() {
            return resultSet -> {
                String id = resultSet.getString("id");
                String deviceId = resultSet.getString("device_id");
                String name = resultSet.getString("name");
                int slaveId = resultSet.getInt("slave_id");
                byte functionCode = resultSet.getByte("function_code");
                int registerStart = resultSet.getInt("register_start");
                String dataFormat = resultSet.getString("data_format");
                String byteOrder = resultSet.getString("byte_order");
                int refreshRate = resultSet.getInt("refresh_rate");

                return new Metric_v1_1(id, name, deviceId, slaveId, functionCode,
                        registerStart, dataFormat, byteOrder, refreshRate);
            };
        }
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    @AllArgsConstructor
    static class MetricData_v1_1 {

        private int id;
        @NonNull
        private String metricId;
        @NonNull
        private byte[] value;
        @NonNull
        private Timestamp timestamp;

        public static StatementProvider selectDataByMetricProvider(String metricId) {
            return connection -> {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM metrics_data WHERE metric_id = ?"
                );
                statement.setString(1, metricId);
                return statement;
            };
        }

        public static ResultParser<MetricData_v1_1> parser() {
            return resultSet -> {
                int id = resultSet.getInt("id");
                String metricId = resultSet.getString("metric_id");
                byte[] value = resultSet.getBytes("value");
                Timestamp timestamp = resultSet.getTimestamp("timestamp");

                return new MetricData_v1_1(id, metricId, value, timestamp);
            };
        }
    }
}
