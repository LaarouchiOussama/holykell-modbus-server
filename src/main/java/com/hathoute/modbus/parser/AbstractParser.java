package com.hathoute.modbus.parser;

import com.hathoute.modbus.Helpers;
import com.hathoute.modbus.exception.ByteLengthMismatchException;
import lombok.AllArgsConstructor;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractParser {
    private static final Logger logger = LoggerFactory.getLogger(AbstractParser.class);

    @AllArgsConstructor
    static class ParserData {
        Constructor<? extends AbstractParser> constructor;
        Parser info;
    }
    private static final Map<String, ParserData> constructors = new HashMap<>();

    protected ByteBufferSupplier byteBufferSupplier;

    protected AbstractParser(ByteBufferSupplier supplier) {
        this.byteBufferSupplier = supplier;
    }

    public double parse(Byte[] value) throws ByteLengthMismatchException {
        return parse(Helpers.unwrap(value));
    }

    public abstract double parse(byte[] value) throws ByteLengthMismatchException;

    public static AbstractParser getParser(String format, String order) {
        if(!constructors.containsKey(format)) {
            throw new IllegalArgumentException("Cannot create a parser for " + format);
        }

        ByteBufferSupplier supplier = Helpers.createByteBufferSupplier(order);

        Constructor<? extends AbstractParser> constructor = constructors.get(format).constructor;
        try {
            return constructor.newInstance(supplier);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initialize() {
        Reflections reflections = new Reflections("com.hathoute.modbus");
        Set<Class<? extends AbstractParser>> classes = reflections.getSubTypesOf(AbstractParser.class);
        classes.forEach(c -> register((Class<? extends AbstractParser>) c));
    }

    public static int getBytesCount(String format) {
        ParserData data = constructors.get(format);
        if(data == null) {
            throw new IllegalArgumentException("Unknown format: " + format);
        }

        return data.info.size();
    }

    private static void register(Class<? extends AbstractParser> klass) {
        Parser parser = klass.getAnnotation(Parser.class);
        if(parser == null) {
            throw new IllegalStateException("Class " + klass.getSimpleName() + " must" +
                    " be annotated with a Parser annotation");
        }

        for (String format : parser.formats()) {
            logger.debug("Binding format '" + format + "' to parser " + klass.getSimpleName());

            if (constructors.containsKey(format)) {
                throw new IllegalStateException("Found duplicate parsers for " +
                        "the same format '" + format + "': " +
                        constructors.get(format).constructor.getDeclaringClass().getSimpleName()
                        + " and " + klass.getSimpleName() + ".");
            }

            try {
                Constructor<? extends AbstractParser> constructor = klass.getDeclaredConstructor(ByteBufferSupplier.class);
                constructors.put(format, new ParserData(constructor, parser));
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Class " + klass.getSimpleName() + "" +
                        " must provide a constructor with a single ByteBufferSupplier parameter.");
            }
        }
    }
}
