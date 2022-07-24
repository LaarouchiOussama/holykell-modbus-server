package com.hathoute.modbus.parser.implementation;

import com.hathoute.modbus.parser.AbstractParser;
import com.hathoute.modbus.parser.ByteBufferSupplier;
import com.hathoute.modbus.parser.Parser;

import java.nio.ByteBuffer;

@Parser(formats = {"int", "int32"}, size = 4)
public class IntegerParser extends AbstractParser<Integer> {

    public IntegerParser(ByteBufferSupplier supplier) {
        super(supplier);
    }

    @Override
    public Integer parse(byte[] value) {
        ByteBuffer byteBuffer = byteBufferSupplier.get(value);
        return byteBuffer.getInt();
    }
}
