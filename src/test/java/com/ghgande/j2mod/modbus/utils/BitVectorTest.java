package com.ghgande.j2mod.modbus.utils;

import com.ghgande.j2mod.modbus.util.BitVector;
import org.junit.Assert;
import org.junit.Test;

public class BitVectorTest {

    @Test
    public void testBitVector() {
        for (int s = 1; s <= 128; s++) {
            BitVector bv = new BitVector(s);
            Assert.assertNotNull("Could not instantiate bitvector of size " + s, bv);
            Assert.assertEquals("Bitvector does not have size " + s, s, bv.size());
        }
    }

    @Test
    public void testCreateBitVector() {
        byte[] testData = new byte[2];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte)i;
        }
        BitVector b1 = BitVector.createBitVector(testData);
        Assert.assertNotNull("Could not instantiate bitvector of size 16", b1);
        Assert.assertEquals("Bitvector does not have size 16", 16, b1.size());
        BitVector b2 = BitVector.createBitVector(testData, 8);
        Assert.assertNotNull("Could not instantiate bitvector of size 8", b2);
        Assert.assertEquals("Bitvector does not have size 8", 8, b2.size());
    }

    @Test
    public void testMSBLSBAccess() {
        BitVector bv = new BitVector(1);
        Assert.assertFalse("New bitvector instance should have LSB access", bv.isMSBAccess());
        Assert.assertTrue("LSB access should return true", bv.isLSBAccess());
        bv.toggleAccess(true);
        Assert.assertTrue("Bitvector instance cannot toggle to MSB access", bv.isMSBAccess());
        Assert.assertFalse("LSB access should return false", bv.isLSBAccess());
        bv.toggleAccess(false);
        Assert.assertFalse("Bitvector instance cannot toggle to LSB access", bv.isMSBAccess());
        Assert.assertTrue("LSB access should return true", bv.isLSBAccess());
    }

    @Test
    public void testGetSetBytes() {
        byte[] testData = new byte[8];
        byte[] nullData = new byte[8];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte)i;
            nullData[i] = (byte)0;
        }
        BitVector b1 = BitVector.createBitVector(nullData);
        b1.setBytes(testData);
        byte[] actualData = b1.getBytes();
        Assert.assertNotNull("Cannot retrieve bytes from bitvector", actualData);
        Assert.assertEquals("Returned data array does not have the same length as original", testData.length, actualData.length);
        for (int i = 0; i < testData.length; i++) {
            Assert.assertEquals("Byte " + i + " is not equal to testdata", testData[i], actualData[i]);
        }
    }

    @Test
    public void testSetGetBit() {
        byte[] nullData = new byte[8];
        for (int i = 0; i < nullData.length; i++) {
            nullData[i] = (byte)0;
        }
        BitVector bv = BitVector.createBitVector(nullData);
        for (int i = 0; i < 64; i++) {
            Assert.assertFalse("Bit " + i + " should not be set", bv.getBit(i));
            bv.setBit(i, true);
            Assert.assertTrue("Bit " + i + " should be set", bv.getBit(i));
        }
    }

    @Test
    public void testSizes() {
        BitVector b1 = new BitVector(16);
        Assert.assertEquals("Size should be 16", 16, b1.size());
        Assert.assertEquals("Bytesize should be 2", 2, b1.byteSize());
        b1.forceSize(4);
        Assert.assertEquals("Size should be 4", 4, b1.size());
        Assert.assertEquals("Bytesize should still be 2", 2, b1.byteSize());

        BitVector b2 = new BitVector(4);
        Assert.assertEquals("Size should be 4", 4, b2.size());
        Assert.assertEquals("Bytesize should still be 1", 1, b2.byteSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForceIllegalSize() throws IllegalArgumentException {
        BitVector bv = new BitVector(8);
        bv.forceSize(8000);
    }

    @Test
    public void testToString() {
        byte[] testData = new byte[8];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte)i;
        }
        BitVector bv = BitVector.createBitVector(testData);
        bv.forceSize(62);
        Assert.assertEquals("BitVector string is incorrect",
                "00000000 00000001 00000010 00000011 00000100 00000101 00000110 000111 ", bv.toString());
    }
}
