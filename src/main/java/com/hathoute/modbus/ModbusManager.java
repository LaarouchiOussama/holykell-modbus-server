package com.hathoute.modbus;

import com.ghgande.j2mod.modbus.facade.ModbusListenerCallback;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.hathoute.modbus.config.ConfigManager;
import com.hathoute.modbus.database.DatabaseManager;
import com.hathoute.modbus.database.ResultParser;
import com.hathoute.modbus.entity.Device;
import com.hathoute.modbus.entity.Metric;
import com.hathoute.modbus.entity.MetricData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModbusManager {

    private static final Logger logger = LoggerFactory.getLogger(ModbusManager.class);

    // How often do we check the database for new/modified devices.
    private static final int DATABASE_CHECK_SECONDS =
            ConfigManager.getInstance().getIntProperty("database.synchronization_interval");

    static class ModbusEntry {
        public String serialId;
        public ModbusTCPMaster master;
        public Thread thread;
    }

    private final Map<String, Device> devicesBySerialId = new Hashtable<>();
    private final Map<String, ModbusEntry> entryById = new Hashtable<>();

    private final ExecutorService ioExecutor;
    private final DatabaseManager databaseManager;

    private final ScheduledExecutorService databaseUpdateExecutor;

    /**
     * Constructs a new <tt>ModbusManager</tt> instance. <br/> Calling this constructor will
     * immediately send queries to the database provided by the databaseManager parameter, ensuring
     * that the database is well configured <b>is not</b> this class's business.
     *
     * @param databaseManager The database manager
     */
    public ModbusManager(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        ioExecutor = Executors.newSingleThreadExecutor();
        initialize();

        databaseUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        databaseUpdateExecutor.scheduleAtFixedRate(this::checkDatabase, DATABASE_CHECK_SECONDS,
            DATABASE_CHECK_SECONDS, TimeUnit.SECONDS);
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
            logger.trace("callback.return({}, master)", serialId);
            if (!devicesBySerialId.containsKey(serialId)) {
                logger.warn("Unrecognized serial id '{}' tried to connect.", serialId);
                master.disconnect();
                return;
            }

            final ModbusEntry entry = new ModbusEntry();
            entry.master = master;
            entry.serialId = serialId;

            final Device device = devicesBySerialId.get(serialId);
            entry.thread = new ModbusConnectionThread(this, master, device);

            addEntry(entry);
        };
    }

    public void saveMetricData(final MetricData data) {
        logger.trace("saveMetricData(data)");
        ioExecutor.execute(() -> {
            try {
                databaseManager.query(data.insertDataProvider());
                logger.trace("Metric Data Saved");
            } catch (final SQLException e) {
                logger.error("Error while saving metric data: ", e);
            }
        });
    }

    private void initialize() {
        logger.trace("initialize()");

        try {
            final List<Device> devices = databaseManager.query(Device.selectAllDevicesProvider(),
                ResultParser.listParser(Device.parser()));

            logger.info("Found " + devices.size() + " devices:");
            for (final Device device : devices) {
                logger.info("\t" + device.getName() + " (SID: " + device.getSerialId() + ")");
                devicesBySerialId.put(device.getSerialId(), device);

                final List<Metric> metrics = databaseManager.query(
                    Metric.selectMetricByDeviceProvider(device.getId()),
                    ResultParser.listParser(Metric.parser()));
                for (final Metric metric : metrics) {
                    logger.info("\t\t" + metric.getName());
                    device.addMetric(metric);
                }
            }

        } catch (final SQLException e) {
            // TODO: Handle exception...
            throw new RuntimeException(e);
        }
    }

    private void checkDatabase() {
        logger.trace("checkDatabase()");
        ioExecutor.execute(() -> {
            logger.trace("checkDatabase() - ioExecutor.execute(...)");

            try {
                final List<Device> devices = databaseManager.query(
                    Device.selectAllDevicesProvider(),
                    ResultParser.listParser(Device.parser()));

                for (final Device device : devices) {
                    final List<Metric> metrics = databaseManager.query(
                        Metric.selectMetricByDeviceProvider(device.getId()),
                        ResultParser.listParser(Metric.parser()));
                    metrics.forEach(device::addMetric);

                    if (!devicesBySerialId.containsKey(device.getSerialId())) {
                        // New device detected
                        devicesBySerialId.put(device.getSerialId(), device);
                        logger.info(String.format("Found new device %s (SID: %s)",
                            device.getName(), device.getSerialId()));
                        metrics.forEach(m -> {
                            logger.info(String.format("   - %s", m.getName()));
                        });
                        continue;
                    }

                    // Get existing device and check metrics
                    final Device existing = devicesBySerialId.get(device.getSerialId());
                    final Map<Integer, Metric> existingMetrics = existing.getMetrics().stream()
                        .collect(Collectors.toMap(Metric::getId, x -> x));

                    boolean modified = false;

                    // Check if there's any new/edited metric
                    for (final Metric newMetric : device.getMetrics()) {
                        final Metric existingMetric = existingMetrics.get(newMetric.getId());
                        if (existingMetric == null) {
                            // New metric detected
                            logger.info(String.format("New metric detected for device %s:    %s",
                                device.getName(), newMetric.getName()));
                            modified = true;
                            continue;
                        }

                        existingMetrics.remove(existingMetric.getId());
                        if (!existingMetric.equals(newMetric)) {
                            // Modified metric
                            logger.info(
                                String.format("Modified metric detected for device %s:    %s",
                                    device.getName(), newMetric.getName()));
                            modified = true;
                        }
                    }

                    for (final Metric deletedMetric : existingMetrics.values()) {
                        logger.info(String.format("Deleted metric detected for device %s:    %s",
                            device.getName(), deletedMetric.getName()));
                        modified = true;
                    }

                    if (modified) {
                        devicesBySerialId.put(device.getSerialId(), device);
                        final ModbusEntry entry = entryById.get(device.getSerialId());
                        if (entry != null) {
                            // There is already an existing connection to this device,
                            // drop this connection and wait for device to reconnect.
                            logger.info(String.format("Device %s was modified, dropping existing connection.",
                                    device.getName()));

                            // Interrupt running thread then disconnect master,
                            // since upon interrupting there might be running tasks.
                            entry.thread.interrupt();
                            try {
                                // Wait for thread to fully terminate
                                entry.thread.join(1000);
                            } catch (final InterruptedException e) {
                                // Do nothing...
                            }
                            entry.master.disconnect();
                            entryById.remove(entry.serialId);
                        }
                    }
                }
            } catch (final SQLException e) {
                logger.error("checkDatabase failed", e);
            }
        });
    }

    private void addEntry(final ModbusEntry entry) {
        synchronized (entryById) {
            logger.trace("addEntry({})", entry.serialId);
            final ModbusEntry existingEntry = entryById.get(entry.serialId);
            if (existingEntry != null) {
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