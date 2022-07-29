package com.hathoute.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.hathoute.modbus.config.ConfigManager;
import com.hathoute.modbus.entity.Device;
import com.hathoute.modbus.entity.Metric;
import com.hathoute.modbus.entity.MetricData;
import com.hathoute.modbus.functioncode.ModbusOperation;
import com.hathoute.modbus.functioncode.ModbusOperationFactory;
import com.hathoute.modbus.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ModbusConnectionThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ModbusConnectionThread.class);

    private final ModbusManager modbusManager;
    private final ModbusTCPMaster master;
    private final Device device;

    public ModbusConnectionThread(ModbusManager modbusManager, ModbusTCPMaster master, Device device) {
        this.modbusManager = modbusManager;
        this.master = master;
        this.device = device;
    }

    @Override
    public void run() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        for (Metric metric : device.getMetrics()) {
            ModbusMetricRunnable mmr = new ModbusMetricRunnable(metric);
            executor.scheduleAtFixedRate(mmr, 0, metric.getRefreshRate(), TimeUnit.SECONDS);
        }

        while (!Thread.interrupted()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    class ModbusMetricRunnable implements Runnable {
        private final Metric metric;
        private final ModbusOperation handler;
        private final AbstractParser<?> parser;

        public ModbusMetricRunnable(Metric metric) {
            this.metric = metric;
            this.handler = ModbusOperationFactory.create(metric);
            this.parser = AbstractParser.getParser(metric.getDataFormat(), metric.getByteOrder());
        }

        @Override
        public void run() {
            try {
                byte[] bytes = handler.execute(master);
                MetricData data = new MetricData(metric.getId(), bytes, Timestamp.from(Instant.now()));
                modbusManager.saveMetricData(data);

                if(ConfigManager.getInstance().getBooleanProperty("debug.show_value")) {
                    logger.info("Metric '" + metric.getName() + "' (" + metric.getId()
                            + ") : " + parser.parse(bytes));
                }
            } catch (ModbusException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
