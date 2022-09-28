package com.hathoute.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.hathoute.modbus.config.ConfigManager;
import com.hathoute.modbus.entity.Device;
import com.hathoute.modbus.entity.Metric;
import com.hathoute.modbus.entity.MetricData;
import com.hathoute.modbus.exception.ByteLengthMismatchException;
import com.hathoute.modbus.functioncode.ModbusOperation;
import com.hathoute.modbus.functioncode.ModbusOperationFactory;
import com.hathoute.modbus.parser.AbstractParser;
import java.io.InputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModbusConnectionThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ModbusConnectionThread.class);

    private final ModbusManager modbusManager;
    private final ModbusTCPMaster master;
    private final Device device;
    private final AtomicBoolean terminateRequested;

    public ModbusConnectionThread(final ModbusManager modbusManager, final ModbusTCPMaster master,
        final Device device) {
        this.modbusManager = modbusManager;
        this.master = master;
        this.device = device;
        terminateRequested = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        final List<ScheduledFuture<?>> futures = new ArrayList<>();
        for (final Metric metric : device.getMetrics()) {
            final ModbusMetricRunnable mmr = new ModbusMetricRunnable(metric);
            final ScheduledFuture<?> future = executor.scheduleAtFixedRate(mmr, 0,
                metric.getRefreshRate(), TimeUnit.SECONDS);
            futures.add(future);
        }

        while (!Thread.interrupted() && !terminateRequested.get()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (final InterruptedException e) {
                break;
            }
        }

        if (terminateRequested.get()) {
            logger.debug("Thread termination requested for device {}", device.getName());

            if (ConfigManager.getInstance().getBooleanProperty("server.shutdown_on_critical")) {
                logger.debug("Shutting down due to a critical error");
                System.exit(51);
            }
        }

        logger.debug("Terminating all executors for device {}", device.getName());
        futures.forEach(f -> f.cancel(false));

        // Make sure we don't have any hanging connection...
        master.disconnect();
    }

    class ModbusMetricRunnable implements Runnable {
        private final Metric metric;
        private final ModbusOperation handler;
        private final AbstractParser parser;

        public ModbusMetricRunnable(final Metric metric) {
            this.metric = metric;
            handler = ModbusOperationFactory.create(metric);
            parser = AbstractParser.getParser(metric.getDataFormat(), metric.getByteOrder());
        }

        @Override
        public void run() {
            try {
                // Deal with heartbeats
                final Socket client = master.getConnection().getSocket();
                final InputStream is = client.getInputStream();
                if (is.available() > 0) {
                    logger.debug("Found {} bytes tailing", is.available());
                    final byte[] tail = new byte[is.available()];
                    final int read = is.read(tail);
                    logger.debug("Bytes read: {}, size: {}", tail, read);
                }

                final byte[] bytes = handler.execute(master);
                final double value = parser.parse(bytes);
                final MetricData data = new MetricData(metric.getId(), value,
                    Timestamp.from(Instant.now()));
                modbusManager.saveMetricData(data);

                if (ConfigManager.getInstance().getBooleanProperty("debug.show_value")) {
                    logger.debug("Metric '{}' : {} {}", metric.getName(), value, metric.getUnit());
                }
            } catch (final ByteLengthMismatchException e) {
                logger.error("Corrupted value for metric " + metric.getName());
            } catch (final ModbusException e) {
                logger.error("ModbusException inside ModbusMetricRunnable", e);
            } catch (final Exception e) {
                terminateRequested.set(true);
                logger.error("Critical error inside ModbusMetricRunnable", e);
            }
        }
    }
}
