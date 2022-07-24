package com.hathoute.modbus.functioncode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionCode {
    byte value();
}
