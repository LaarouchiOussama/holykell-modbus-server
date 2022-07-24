package com.hathoute.modbus.config;

import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

public class ConfigManager {

    private static ConfigManager instance;
    public static ConfigManager getInstance() {
        if(instance == null) {
            instance = new ConfigManager();
        }

        return instance;
    }

    private static final String propertiesFilename = "modbus-server.properties";
    private final Properties properties = new Properties();

    private ConfigManager() {
        try {
            loadProperties();
        } catch (IOException e) {
            throw new RuntimeException("Exception while initializing ConfigManager: ", e);
        }
    }

    public String getProperty(String propertyName) {
        String property = System.getenv(toEnvironmentVariable(propertyName));
        if(property != null) {
            return property;
        }

        property = properties.getProperty(propertyName);
        if(property != null) {
            return property;
        }

        throw new RuntimeException("Property '" + propertyName + "' not found.");
    }

    public boolean getBooleanProperty(String propertyName) {
        String property = getProperty(propertyName);
        return Boolean.parseBoolean(property);
    }

    private void loadProperties() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFilename);

        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propertiesFilename + "' not found in the classpath");
        }
    }

    private String toEnvironmentVariable(String property) {
        return property.toUpperCase().replace(".", "_");
    }
}
