package com.hathoute.modbus.parser;

import java.nio.ByteBuffer;

public interface ByteBufferSupplier {
    ByteBuffer get(byte[] bytes);
}
