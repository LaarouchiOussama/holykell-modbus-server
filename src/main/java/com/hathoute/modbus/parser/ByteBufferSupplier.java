package com.hathoute.modbus.parser;

import com.hathoute.modbus.exception.ByteLengthMismatchException;

import java.nio.ByteBuffer;

public interface ByteBufferSupplier {
    ByteBuffer get(byte[] bytes) throws ByteLengthMismatchException;
}
