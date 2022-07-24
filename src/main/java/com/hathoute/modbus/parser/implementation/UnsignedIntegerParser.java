package com.hathoute.modbus.parser.implementation;

import com.hathoute.modbus.parser.AbstractParser;
import com.hathoute.modbus.parser.ByteBufferSupplier;
import com.hathoute.modbus.parser.Parser;

import java.nio.ByteBuffer;

@Parser(formats = {"uint", "uint32"}, size = 4)
public class UnsignedIntegerParser extends AbstractParser<Long> {

    public UnsignedIntegerParser(ByteBufferSupplier supplier) {
        super(supplier);
    }

    @Override
    public Long parse(byte[] value) {
        ByteBuffer byteBuffer = byteBufferSupplier.get(value);
        int signed = byteBuffer.getInt();
        return Integer.toUnsignedLong(signed);
    }
}
