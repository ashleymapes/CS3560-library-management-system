package com.cpp.library.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static Properties properties;
    private static Dotenv dotenv;
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        properties = new Properties();
        
        // Try to load .env file, but don't fail if it doesn't exist
        try {
            dotenv = Dotenv.load();
        } catch (Exception e) {
            System.out.println("No .env file found, using properties file only");
            dotenv = null;
        }
        
        // Load from application.properties
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Error loading application.properties: " + e.getMessage());
        }
        
        // Load from env.properties if it exists
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("env.properties")) {
            if (input != null) {
                Properties envProps = new Properties();
                envProps.load(input);
                properties.putAll(envProps);
            }
        } catch (IOException e) {
            System.err.println("Error loading env.properties: " + e.getMessage());
        }
    }
    
    public static String getProperty(String key) {
        // First try environment variable
        if (dotenv != null) {
            String envValue = dotenv.get(key);
            if (envValue != null) {
                return envValue;
            }
        }
        
        // Then try properties file
        return properties.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing integer property " + key + ": " + e.getMessage());
            }
        }
        return defaultValue;
    }
    
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
} 