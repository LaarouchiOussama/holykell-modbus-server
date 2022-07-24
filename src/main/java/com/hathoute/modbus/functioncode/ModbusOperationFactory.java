package com.hathoute.modbus.functioncode;

import com.hathoute.modbus.entity.Metric;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModbusOperationFactory {

    private static final Map<Byte, Constructor<? extends ModbusOperation>> constructors = new HashMap<>();

    public static void initialize() {
        Reflections reflections = new Reflections("com.hathoute.modbus");
        Set<Class<? extends ModbusOperation>> classes = reflections.getSubTypesOf(ModbusOperation.class);
        for (Class<? extends ModbusOperation> c : classes) {
            FunctionCode fc = c.getAnnotation(FunctionCode.class);
            if(fc == null) {
                throw new IllegalStateException("Class " + c.getSimpleName()
                        + " must be annotated with a FunctionCode annotation.");
            }

            Byte fcId = fc.value();
            if (constructors.containsKey(fcId)) {
                throw new IllegalStateException("Found duplicate handlers for " +
                        "the same function code '" + fcId + "': " +
                        constructors.get(fcId).getDeclaringClass().getSimpleName()
                        + " and " + c.getSimpleName() + ".");
            }

            try {
                Constructor<? extends ModbusOperation> constructor = c.getDeclaredConstructor();
                constructors.put(fcId, constructor);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Class " + c.getSimpleName() + "" +
                        " must provide a default constructor with no parameters.");
            }
        }
    }

    public static ModbusOperation create(Metric metric) {
        Constructor<? extends ModbusOperation> constructor = constructors.get(metric.getFunctionCode());
        if(constructor == null) {
            throw new IllegalArgumentException("Cannot find a handler for function code: " + metric.getFunctionCode());
        }

        ModbusOperation handler;
        try {
            handler = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        handler.configure(metric);
        return handler;
    }
}
