package com.hathoute.modbus;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.hathoute.modbus.config.ConfigManager;
import com.hathoute.modbus.database.DatabaseManager;
import com.hathoute.modbus.database.migration.MigrationManager;
import com.hathoute.modbus.functioncode.ModbusOperationFactory;
import com.hathoute.modbus.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    protected static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Initialize
        AbstractParser.initialize();
        ModbusOperationFactory.initialize();

        logger.info("Running version {} of modbus-rtu-server",
            Main.class.getPackage().getImplementationVersion());

        // Check if we're running a command
        if(args.length > 0) {
            switch (args[0]) {
                case "run_migrations":
                    if (args.length < 3) {
                        logger.error("Wrong usage of run_migrations, must provide version bounds.");
                        logger.error("Example: 'java app.jar run_migrations 1.0.0 2.0.0'");
                    }

                    MigrationManager.runMigrations(args[1], args[2]);
                    return;
                default:
                    logger.error("Unknown command: " + args[0]);
                    return;
            }
        }

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
