package com.hathoute.modbus.parser.implementation;

import com.hathoute.modbus.parser.AbstractParser;
import com.hathoute.modbus.parser.ByteBufferSupplier;
import com.hathoute.modbus.parser.Parser;

import java.nio.ByteBuffer;

@Parser(formats = {"ushort", "uint16"}, size = 2)
public class UnsignedShortParser extends AbstractParser<Integer> {

    public UnsignedShortParser(ByteBufferSupplier supplier) {
        super(supplier);
    }

    @Override
    public Integer parse(byte[] value) {
        ByteBuffer byteBuffer = byteBufferSupplier.get(value);
        short signed = byteBuffer.getShort();
        return Short.toUnsignedInt(signed);
    }
}
