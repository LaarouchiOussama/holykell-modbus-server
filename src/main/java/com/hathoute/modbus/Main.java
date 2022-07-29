package com.hathoute.modbus;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.hathoute.modbus.config.ConfigManager;
import com.hathoute.modbus.database.DatabaseManager;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        ModbusManager modbusManager;

        try {
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.tryInitialize();

            modbusManager = new ModbusManager(databaseManager);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to start Modbus Server:", e);
        }

        try {
            int port = ConfigManager.getInstance().getIntProperty("server.port");
            boolean useRtuOverTcp = ConfigManager.getInstance().getBooleanProperty("server.use_rtu_over_tcp");
            ModbusTCPMaster.listen(port, useRtuOverTcp, modbusManager.callback());
        } catch (IOException e) {
            // TODO: Handle exceptions so that the program never halts...
            throw new RuntimeException(e);
        }
    }
}
