package com.ghgande.j2mod.modbus;

import org.junit.Assert;
import org.junit.Test;

public class ModbusExceptionsTest {

    @Test
    public void testModbusException() {
        Assert.assertNotNull("Instantiation of ModbusException() failed.", new ModbusException());
        ModbusException modbusEx1 = new ModbusException("TEST1");
        Assert.assertEquals("Exception message incorrect return.", "TEST1", modbusEx1.getMessage());
        ModbusException modbusEx2 = new ModbusException("TEST2", modbusEx1);
        Assert.assertEquals("Exception message incorrect.", "TEST2", modbusEx2.getMessage());
        Assert.assertEquals("Exception cause incorrect.", modbusEx1, modbusEx2.getCause());
        ModbusException modbusEx3 = new ModbusException("TEST %s %d", "with string formatter", 68);
        Assert.assertEquals("Exception message incorrect.", "TEST with string formatter 68", modbusEx3.getMessage());
    }

    @Test
    public void testModbusIOException() {
        Assert.assertNotNull("Instantiation of ModbusIOException() failed.", new ModbusIOException());
        ModbusIOException modbusEx1 = new ModbusIOException("TEST1");
        Assert.assertEquals("Exception message incorrect return.", "TEST1", modbusEx1.getMessage());
        ModbusIOException modbusEx2 = new ModbusIOException("TEST2", modbusEx1);
        Assert.assertEquals("Exception message incorrect.", "TEST2", modbusEx2.getMessage());
        Assert.assertEquals("Exception cause incorrect.", modbusEx1, modbusEx2.getCause());
        ModbusIOException modbusEx3 = new ModbusIOException("TEST %s %d", "with string formatter", 68);
        Assert.assertEquals("Exception message incorrect.", "TEST with string formatter 68", modbusEx3.getMessage());

        ModbusIOException modbusEx4 = new ModbusIOException(true);
        Assert.assertTrue("IO Exception should be at EOF.", modbusEx4.isEOF());
        modbusEx4.setEOF(false);
        Assert.assertFalse("IO Exception should not be at EOF.", modbusEx4.isEOF());
    }

    @Test
    public void testModbusSlaveException() {
        for (int i = 0; i < 20; i++) {
            ModbusSlaveException modbusEx = new ModbusSlaveException(i);
            Assert.assertEquals("Incorrect type for " + i, i, modbusEx.getType());
            Assert.assertEquals("Incorrect type string for " + i,
                    ModbusSlaveException.getMessage(i), modbusEx.getMessage());
            Assert.assertTrue("Incorrect type check for " + i, modbusEx.isType(i));
            Assert.assertFalse("Incorrect negative type check for " + i, modbusEx.isType(-1));
        }
    }
}
