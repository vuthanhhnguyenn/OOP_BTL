package com.clinic.triage.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {
    private static final String DB_ENV = "CLINIC_DB_PATH";
    private static final String DEFAULT_DB_RELATIVE = "database/clinic-triage.db";
    private static final String CONFIG_FILE = "config/app.properties";
    private static final String DB_PROPERTY = "database.path";

    private static Properties properties;

    private AppConfig() {
    }

    public static String getDatabasePath() {
        String override = System.getenv(DB_ENV);
        if (override != null && !override.isBlank()) {
            return override.trim();
        }
        String propertyValue = getProperties().getProperty(DB_PROPERTY);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return toAbsolutePath(propertyValue.trim());
        }
        return toAbsolutePath(DEFAULT_DB_RELATIVE);
    }

    public static String getDatabaseUrl() {
        return "jdbc:sqlite:" + getDatabasePath();
    }

    private static synchronized Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            Path configPath = Path.of(CONFIG_FILE);
            if (Files.exists(configPath)) {
                try (InputStream inputStream = Files.newInputStream(configPath)) {
                    properties.load(inputStream);
                } catch (IOException ignored) {
                    
                }
            }
        }
        return properties;
    }

    private static String toAbsolutePath(String configuredPath) {
        Path path = Path.of(configuredPath);
        if (!path.isAbsolute()) {
            path = Path.of("").resolve(path).toAbsolutePath();
        }
        try {
            Files.createDirectories(path.getParent());
        } catch (Exception ignored) {
        }
        return path.toString();
    }
}
