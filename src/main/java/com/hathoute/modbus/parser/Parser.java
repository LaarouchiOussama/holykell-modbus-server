package com.hathoute.modbus.parser;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Parser {
    String[] formats();
    int size();
}
