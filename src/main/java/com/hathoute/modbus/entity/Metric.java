package com.hathoute.modbus.entity;


import com.hathoute.modbus.database.ResultParser;
import com.hathoute.modbus.database.StatementProvider;
import lombok.*;

import java.sql.PreparedStatement;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Metric {

    private int id;
    @NonNull
    private String name;
    private int deviceId;
    private int slaveId;
    private byte functionCode;
    private int registerStart;
    @NonNull
    private String dataFormat;
    private String byteOrder;
    private int refreshRate;

    public static StatementProvider selectMetricByDeviceProvider(int deviceId) {
        return connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM metrics WHERE device_id = ?"
            );
            statement.setInt(1, deviceId);
            return statement;
        };
    }

    public static ResultParser<Metric> parser() {
        return resultSet -> {
            int id = resultSet.getInt("id");
            int deviceId = resultSet.getInt("device_id");
            String name = resultSet.getString("name");
            int slaveId = resultSet.getInt("slave_id");
            byte functionCode = resultSet.getByte("function_code");
            int registerStart = resultSet.getInt("register_start");
            String dataFormat = resultSet.getString("data_format");
            String byteOrder = resultSet.getString("byte_order");
            int refreshRate = resultSet.getInt("refresh_rate");

            return new Metric(id, name, deviceId, slaveId, functionCode,
                    registerStart, dataFormat, byteOrder, refreshRate);
        };
    }
}
