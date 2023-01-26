package com.hathoute.modbus.entity;

import com.hathoute.modbus.database.ResultParser;
import com.hathoute.modbus.database.StatementProvider;
import lombok.*;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class MetricData {

    private int id;
    @NonNull
    private String metricId;
    @NonNull
    private double value;
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

    public StatementProvider insertDataProvider() {
        return connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO metrics_data(metric_id, value, timestamp) " +
                            "VALUES (?, ?, ?);"
            );
            statement.setString(1, this.metricId);
            statement.setDouble(2, this.value);
            statement.setTimestamp(3, this.timestamp);

            return statement;
        };
    }

    public static ResultParser<MetricData> parser() {
        return resultSet -> {
            int id = resultSet.getInt("id");
            String metricId = resultSet.getString("metric_id");
            double value = resultSet.getDouble("value");
            Timestamp timestamp = resultSet.getTimestamp("timestamp");

            return new MetricData(id, metricId, value, timestamp);
        };
    }
}
