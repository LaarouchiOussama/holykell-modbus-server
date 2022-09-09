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
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

public class ModbusConnectionThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ModbusConnectionThread.class);

    private final ModbusManager modbusManager;
    private final ModbusTCPMaster master;
    private final Device device;
    private final AtomicBoolean terminateRequested;

    public ModbusConnectionThread(ModbusManager modbusManager, ModbusTCPMaster master, Device device) {
        this.modbusManager = modbusManager;
        this.master = master;
        this.device = device;
        this.terminateRequested = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        List<ScheduledFuture<?>> futures = new ArrayList<>();
        for (Metric metric : device.getMetrics()) {
            ModbusMetricRunnable mmr = new ModbusMetricRunnable(metric);
            ScheduledFuture<?> future = executor.scheduleAtFixedRate(mmr, 0, metric.getRefreshRate(), TimeUnit.SECONDS);
            futures.add(future);
        }

        while (!Thread.interrupted() && !terminateRequested.get()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                break;
            }
        }

        if (terminateRequested.get()) {
            logger.debug("Thread termination requested for device {}", device.getName());
        }

        logger.debug("Terminating all executors for device {}", device.getName());
        for (ScheduledFuture<?> future : futures) {
            future.cancel(false);
        }
    }

    class ModbusMetricRunnable implements Runnable {
        private final Metric metric;
        private final ModbusOperation handler;
        private final AbstractParser parser;

        public ModbusMetricRunnable(Metric metric) {
            this.metric = metric;
            this.handler = ModbusOperationFactory.create(metric);
            this.parser = AbstractParser.getParser(metric.getDataFormat(), metric.getByteOrder());
        }

        @Override
        public void run() {
            try {
                // Deal with heartbeats
                Socket client = master.getConnection().getSocket();
                InputStream is = client.getInputStream();
                if(is.available() > 0) {
                    logger.debug("Found {} bytes tailing", is.available());
                    byte[] tail = new byte[is.available()];
                    int read = is.read(tail);
                    logger.debug("Bytes read: {}, size: {}", tail, read);
                }

                byte[] bytes = handler.execute(master);
                double value = parser.parse(bytes);
                MetricData data = new MetricData(metric.getId(), value, Timestamp.from(Instant.now()));
                modbusManager.saveMetricData(data);

                if(ConfigManager.getInstance().getBooleanProperty("debug.show_value")) {
                    logger.debug("Metric '" + metric.getName() + "' : " + value + " " + metric.getUnit());
                }
            } catch (ByteLengthMismatchException e) {
                logger.error("Corrupted value for metric " + metric.getName());
            } catch (ModbusException e) {
                logger.error("ModbusException inside ModbusMetricRunnable", e);
            } catch (Exception e) {
                terminateRequested.set(true);
                logger.error("Critical error inside ModbusMetricRunnable", e);
            }
        }
    }
}
