package com.hathoute.modbus.functioncode;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.hathoute.modbus.Helpers;
import com.hathoute.modbus.entity.Metric;
import com.hathoute.modbus.parser.AbstractParser;

import java.util.Arrays;

@FunctionCode(3)
public class ReadRegisterOperation implements ModbusOperation {

    private int slaveId;
    private int registerStart;
    private int dataSize;

    @Override
    public void configure(Metric metric) {
        this.slaveId = metric.getSlaveId();
        this.registerStart = metric.getRegisterStart();
        this.dataSize = AbstractParser.getBytesCount(metric.getDataFormat()) / 2;
    }

    @Override
    public byte[] execute(ModbusTCPMaster master) throws ModbusException {
        Register[] registers = master.readMultipleRegisters(slaveId, registerStart, dataSize);

        final Byte[] bytes = new Byte[registers.length * 2];
        Arrays.setAll(bytes, i -> registers[i/2].toBytes()[i%2]);

        return Helpers.unwrap(bytes);
    }
}
