package com.hathoute.modbus.functioncode;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.hathoute.modbus.entity.Metric;

public interface ModbusOperation {
    void configure(Metric metric);
    byte[] execute(ModbusTCPMaster master) throws ModbusException;
}
