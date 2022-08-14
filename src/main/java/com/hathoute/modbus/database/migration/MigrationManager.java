package com.hathoute.modbus.database.migration;

import com.hathoute.modbus.database.DatabaseManager;
import com.hathoute.modbus.functioncode.FunctionCode;
import com.hathoute.modbus.functioncode.ModbusOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MigrationManager {

    protected static Logger logger = LoggerFactory.getLogger(MigrationManager.class);

    public static void runMigrations(String databaseVersion, String toVersion) {

        logger.info("Searching for migrations from " + databaseVersion + " to " + toVersion);

        Version from = Version.parse(databaseVersion);
        Version to = Version.parse(toVersion);

        Reflections reflections = new Reflections("com.hathoute.modbus");
        Set<Class<? extends DatabaseMigration>> classes = reflections.getSubTypesOf(DatabaseMigration.class);
        List<DatabaseMigration> migrations = new ArrayList<>();
        for (Class<? extends DatabaseMigration> c : classes) {
            try {
                DatabaseMigration dm = c.getConstructor().newInstance();
                Version dmVersion = Version.parse(dm.version());

                if(dmVersion.compareTo(from) > 0 && dmVersion.compareTo(to) <= 0) {
                    migrations.add(dm);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        migrations.sort(Comparator.comparing(o -> Version.parse(o.version())));
        logger.info("Found " + migrations.size() + " migration(s) to execute.");
        for (DatabaseMigration dm : migrations) {
            logger.info("    Migration to version " + dm.version());
        }

        DatabaseManager databaseManager = new DatabaseManager();
        try {
            databaseManager.tryInitialize();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run migrations", e);
        }
        try {
            for (DatabaseMigration dm : migrations) {
                logger.info("Executing migration " + dm.version());
                dm.execute(databaseManager);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run migrations", e);
        }
    }

    @Data
    @AllArgsConstructor
    static class Version implements Comparable<Version> {
        int major;
        int minor;
        int patch;

        static Version parse(String str) {
            Version v = new Version(0, 0, 0);
            String[] vs = str.split("\\.");
            if(vs.length > 0) {
                v.setMajor(Integer.parseInt(vs[0]));
            }
            if(vs.length > 1) {
                v.setMinor(Integer.parseInt(vs[1]));
            }
            if(vs.length > 2) {
                v.setPatch(Integer.parseInt(vs[2]));
            }

            return v;
        }

        @Override
        public int compareTo(Version v) {
            if(this.major != v.major) {
                return this.major - v.major;
            }
            else if(this.minor != v.minor) {
                return this.minor - v.minor;
            }

            return this.patch - v.patch;
        }
    }
}
