package com.hathoute.modbus.entity;

import com.hathoute.modbus.database.ResultParser;
import com.hathoute.modbus.database.StatementProvider;
import lombok.*;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Device {

    @Getter @Setter
    private String id;
    @NonNull
    @Getter @Setter
    private String serialId;
    @NonNull
    @Getter @Setter
    private String name;
    private List<Metric> metrics;

    public Device(String id, @NonNull String serialId, @NonNull String name) {
        this.id = id;
        this.serialId = serialId;
        this.name = name;
        this.metrics = new ArrayList<>();
    }

    public List<Metric> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }

    public void addMetric(Metric metric) {
        if(metric.getDeviceId() != this.id) {
            throw new IllegalStateException("Device id mismatch: metric.deviceId = " + metric.getDeviceId()
                    + " and this.id = " + this.id);
        }

        metrics.add(metric);
    }

    public static StatementProvider selectAllDevicesProvider() {
        return connection -> connection.prepareStatement(
                "SELECT * FROM devices;"
        );
    }

    public static StatementProvider queryDeviceProvider(String deviceId) {
        return connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM devices WHERE id = ?"
            );
            statement.setString(1, deviceId);
            return statement;
        };
    }

    public static ResultParser<Device> parser() {
        return resultSet -> {
            String id = resultSet.getString("id");
            String serialId = resultSet.getString("serial_id");
            String name = resultSet.getString("name");

            return new Device(id, serialId, name);
        };
    }
}
