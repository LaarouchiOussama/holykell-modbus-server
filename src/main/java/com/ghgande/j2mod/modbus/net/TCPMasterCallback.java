package com.ghgande.j2mod.modbus.net;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;

public interface TCPMasterCallback {

    void onConnection(String serialId, TCPMasterConnection connection);

}
