package com.hathoute.modbus;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.hathoute.modbus.database.DatabaseManager;
import com.hathoute.modbus.parser.AbstractParser;

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
            ModbusTCPMaster.listen(6651, true, modbusManager.callback());
        } catch (IOException e) {
            // TODO: Handle exceptions so that the program never halts...
            throw new RuntimeException(e);
        }
    }
}
