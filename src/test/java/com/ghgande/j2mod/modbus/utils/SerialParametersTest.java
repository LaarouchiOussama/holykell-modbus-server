/*
 * Copyright 2002-2022 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghgande.j2mod.modbus.utils;

import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

;

public class SerialParametersTest {

    private SerialParameters parameters = null;

    @Before
    public void setUp() {
        parameters = new SerialParameters();
    }

    @Test
    public void testConstructorsRs485ModeAndParameters() {
        parameters = new SerialParameters();
        // Check default values for parameter not passed through this
        // constructor.
        assertFalse(parameters.getRs485Mode());
        assertFalse(parameters.getRs485EnableTermination());
        assertFalse(parameters.getRs485RxDuringTx());

        parameters = new SerialParameters(
            "foo",
            42000,
            AbstractSerialConnection.FLOW_CONTROL_DISABLED,
            AbstractSerialConnection.FLOW_CONTROL_DISABLED,
            7,
            AbstractSerialConnection.ONE_STOP_BIT,
            AbstractSerialConnection.EVEN_PARITY,
            false);
        assertFalse(parameters.getRs485Mode());
        // Check default values for parameter not passed through this
        // constructor.
        assertFalse(parameters.getRs485EnableTermination());
        assertFalse(parameters.getRs485RxDuringTx());

        parameters = new SerialParameters(
            "foo",
            42000,
            AbstractSerialConnection.FLOW_CONTROL_DISABLED,
            AbstractSerialConnection.FLOW_CONTROL_DISABLED,
            7,
            AbstractSerialConnection.ONE_STOP_BIT,
            AbstractSerialConnection.EVEN_PARITY,
            false,
            // And now the RS-485 parameters
            true,
            false,
            1234,
            5678);
        assertTrue(parameters.getRs485Mode());
        assertFalse(parameters.getRs485TxEnableActiveHigh());
        assertEquals(1234, parameters.getRs485DelayBeforeTxMicroseconds());
        assertEquals(5678, parameters.getRs485DelayAfterTxMicroseconds());
        // Check default values for parameter not passed through this
        // constructor.
        assertFalse(parameters.getRs485EnableTermination());
        assertFalse(parameters.getRs485RxDuringTx());
    }

    @Test
    public void testSetAndGetRs485Mode() {
        parameters.setRs485Mode(true);
        assertTrue(parameters.getRs485Mode());

        parameters.setRs485Mode(false);
        assertFalse(parameters.getRs485Mode());
    }

    @Test
    public void testSetAndGetRs485TxEnableActiveHigh() {
        parameters.setRs485TxEnableActiveHigh(true);
        assertTrue(parameters.getRs485TxEnableActiveHigh());

        parameters.setRs485TxEnableActiveHigh(false);
        assertFalse(parameters.getRs485TxEnableActiveHigh());
    }

    @Test
    public void testSetAndGetRs485EnableTermination() {
        parameters.setRs485EnableTermination(true);
        assertTrue(parameters.getRs485EnableTermination());

        parameters.setRs485EnableTermination(false);
        assertFalse(parameters.getRs485EnableTermination());
    }

    @Test
    public void testSetAndGetRs485RxDuringTx() {
        parameters.setRs485RxDuringTx(true);
        assertTrue(parameters.getRs485RxDuringTx());

        parameters.setRs485RxDuringTx(false);
        assertFalse(parameters.getRs485RxDuringTx());
    }

    @Test
    public void testSetAndGetRs485DelayBeforeTxMicrosecondsInt() {
        try {
            parameters.setRs485DelayBeforeTxMicroseconds(-1);
            fail();
        }
        catch (IllegalArgumentException e) {
            // Expected
        }

        parameters.setRs485DelayBeforeTxMicroseconds(0);
        assertEquals(0, parameters.getRs485DelayBeforeTxMicroseconds());

        parameters.setRs485DelayBeforeTxMicroseconds(42);
        assertEquals(42, parameters.getRs485DelayBeforeTxMicroseconds());
    }

    @Test
    public void testSetAndGetRs485DelayBeforeTxMicrosecondsString() {
        try {
            parameters.setRs485DelayBeforeTxMicroseconds("-1");
            fail();
        }
        catch (IllegalArgumentException e) {
            // Expected
        }

        parameters.setRs485DelayBeforeTxMicroseconds("0");
        assertEquals(0, parameters.getRs485DelayBeforeTxMicroseconds());

        parameters.setRs485DelayBeforeTxMicroseconds("42");
        assertEquals(42, parameters.getRs485DelayBeforeTxMicroseconds());

        // Parsing accepts only decimal integer values.
        try {
            parameters.setRs485DelayBeforeTxMicroseconds("f");
            fail();
        }
        catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test
    public void testSetAndGetRs485DelayAfterTxMicrosecondsInt() {
        try {
            parameters.setRs485DelayAfterTxMicroseconds(-1);
            fail();
        }
        catch (IllegalArgumentException e) {
            // Expected
        }

        parameters.setRs485DelayAfterTxMicroseconds(0);
        assertEquals(0, parameters.getRs485DelayAfterTxMicroseconds());

        parameters.setRs485DelayAfterTxMicroseconds(42);
        assertEquals(42, parameters.getRs485DelayAfterTxMicroseconds());
    }

    @Test
    public void testSetAndGetRs485DelayAfterTxMicrosecondsString() {
        try {
            parameters.setRs485DelayAfterTxMicroseconds("-1");
            fail();
        }
        catch (IllegalArgumentException e) {
            // Expected
        }

        parameters.setRs485DelayAfterTxMicroseconds("0");
        assertEquals(0, parameters.getRs485DelayAfterTxMicroseconds());

        parameters.setRs485DelayAfterTxMicroseconds("42");
        assertEquals(42, parameters.getRs485DelayAfterTxMicroseconds());

        // Parsing accepts only decimal integer values.
        try {
            parameters.setRs485DelayAfterTxMicroseconds("f");
            fail();
        }
        catch (NumberFormatException e) {
            // Expected
        }
    }
}
