package com.hathoute.modbus.parser.implementation;

import com.hathoute.modbus.parser.AbstractParser;
import com.hathoute.modbus.parser.ByteBufferSupplier;
import com.hathoute.modbus.parser.Parser;

import java.nio.ByteBuffer;

@Parser(formats = {"double", "float64"}, size = 8)
public class DoubleParser extends AbstractParser<Double> {

    public DoubleParser(ByteBufferSupplier supplier) {
        super(supplier);
    }

    @Override
    public Double parse(byte[] value) {
        ByteBuffer byteBuffer = byteBufferSupplier.get(value);
        return byteBuffer.getDouble();
    }
}
