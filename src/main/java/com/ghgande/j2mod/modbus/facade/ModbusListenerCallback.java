package com.ghgande.j2mod.modbus.facade;

import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

public interface ModbusListenerCallback {

    void onConnection(String serialId, ModbusTCPMaster master);

}
