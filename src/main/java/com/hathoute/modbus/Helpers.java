package com.hathoute.modbus;

import com.hathoute.modbus.parser.ByteBufferSupplier;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Helpers {

    public static byte[] unwrap(Byte[] wrapped) {
        byte[] unwrapped = new byte[wrapped.length];
        for (int i = 0; i < wrapped.length; i++) {
            unwrapped[i] = wrapped[i];
        }
        return unwrapped;
    }

    public static ByteBufferSupplier createByteBufferSupplier(String order) {
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        switch (order) {
            case "CDAB":
            case "DCBA":
            case "GHEFCDAB":
            case "HGFEDCBA":
                byteOrder = ByteOrder.LITTLE_ENDIAN;
        }

        boolean isReversed = false;
        switch (order) {
            case "BA":
            case "BADC":
            case "DCBA":
            case "BADCFEHG":
            case "HGFEDCBA":
                isReversed = true;
        }

        boolean finalIsReversed = isReversed;
        ByteOrder finalByteOrder = byteOrder;
        return bytes -> {
            if(bytes.length != order.length()) {
                // TODO: Log error...
                return null;
            }

            if(finalIsReversed) {
                Helpers.reverseBytesTwoByTwo(bytes);
            }

            return ByteBuffer.wrap(bytes)
                    .order(finalByteOrder);
        };
    }

    private static void reverseBytesTwoByTwo(byte[] bytes) {
        int i = 1;
        while(i < bytes.length) {
            byte b1 = bytes[i];
            bytes[i] = bytes[i-1];
            bytes[i-1] = b1;
            i += 2;
        }
    }

}
