package com.hathoute.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusListenerCallback;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.hathoute.modbus.database.DatabaseManager;
import com.hathoute.modbus.database.ResultParser;
import com.hathoute.modbus.entity.Device;
import com.hathoute.modbus.entity.Metric;
import com.hathoute.modbus.entity.MetricData;
import com.hathoute.modbus.functioncode.ModbusOperationFactory;
import com.hathoute.modbus.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModbusManager {

    private static Logger logger = LoggerFactory.getLogger(ModbusManager.class);

    static class ModbusEntry {
        public String serialId;
        public ModbusTCPMaster master;
        public Thread thread;
    }

    private final Map<String, Device> devicesBySerialId = new Hashtable<>();
    private final Map<String, ModbusEntry> entryById = new Hashtable<>();

    private final ExecutorService ioExecutor;
    private final DatabaseManager databaseManager;

    /**
     * Constructs a new <tt>ModbusManager</tt> instance. <br/>
     * Calling this constructor will immediately send queries to the
     * database provided by the databaseManager parameter, ensuring that
     * the database is well configured <b>is not</b> this class's business.
     *
     * @param databaseManager The database manager
     */
    public ModbusManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.ioExecutor = Executors.newSingleThreadExecutor();
        initialize();
    }

    /**
     * Creates a <tt>ModbusListenerCallback</tt>. <br/>
     * Upon accepting connection from a modbus client, this
     * callback is invoked and therefore a new thread handling
     * this device is spawned.
     * @return The modbus listener callback
     */
    public ModbusListenerCallback callback() {
        return (serialId, master) -> {
            logger.debug("callback.return(" + serialId + ", master)");
            if(!devicesBySerialId.containsKey(serialId)) {
                logger.warn("Unrecognized serial id '" + serialId + "' tried to connect.");
                master.disconnect();
                return;
            }

            ModbusEntry entry = new ModbusEntry();
            entry.master = master;
            entry.serialId = serialId;

            Device device = devicesBySerialId.get(serialId);
            entry.thread = new ModbusConnectionThread(this, master, device);

            addEntry(entry);
        };
    }

    public void saveMetricData(MetricData data) {
        logger.debug("saveMetricData(data)");
        ioExecutor.execute(() -> {
            try {
                databaseManager.query(data.insertDataProvider());
            } catch (SQLException e) {
                logger.error("Error while saving metric data: ", e);
            }
        });
    }

    private void initialize() {
        logger.debug("initialize()");

        try {
            List<Device> devices = databaseManager.query(Device.selectAllDevicesProvider(),
                    ResultParser.listParser(Device.parser()));

            logger.debug("Found " + devices.size() + " devices:");
            for (Device device : devices) {
                logger.debug("\t" + device.getName() + " (SID: " + device.getSerialId() + ")");
                devicesBySerialId.put(device.getSerialId(), device);

                List<Metric> metrics = databaseManager.query(Metric.selectMetricByDeviceProvider(device.getId()),
                        ResultParser.listParser(Metric.parser()));
                for (Metric metric : metrics) {
                    logger.debug("\t\t" + metric.getName());
                    device.addMetric(metric);
                }
            }

        } catch (SQLException e) {
            // TODO: Handle exception...
            throw new RuntimeException(e);
        }
    }

    private void addEntry(ModbusEntry entry) {
        synchronized (entryById) {
            ModbusEntry existingEntry = entryById.get(entry.serialId);
            if(existingEntry != null) {
                logger.warn("Dropping existing connection to '" + entry.serialId + "'.");
                existingEntry.master.disconnect();
                existingEntry.thread.interrupt();
            }

            entryById.put(entry.serialId, entry);
            logger.info("Starting thread for '" + entry.serialId + "'.");
            entry.thread.start();
        }
    }
}