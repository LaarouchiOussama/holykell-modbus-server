package com.hathoute.modbus.parser.implementation;

import com.hathoute.modbus.parser.AbstractParser;
import com.hathoute.modbus.parser.ByteBufferSupplier;
import com.hathoute.modbus.parser.Parser;

import java.nio.ByteBuffer;

@Parser(formats = {"short", "int16"}, size = 2)
public class ShortParser extends AbstractParser<Short> {

    public ShortParser(ByteBufferSupplier supplier) {
        super(supplier);
    }

    @Override
    public Short parse(byte[] value) {
        ByteBuffer byteBuffer = byteBufferSupplier.get(value);
        return byteBuffer.getShort();
    }
}
