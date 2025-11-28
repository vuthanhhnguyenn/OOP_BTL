package com.clinic.triage.db;

import com.clinic.triage.config.AppConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {
    private static boolean initialized;

    private DatabaseManager() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        return DriverManager.getConnection(AppConfig.getDatabaseUrl());
    }

    private static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Không tìm thấy driver SQLite JDBC. Hãy thêm thư viện sqlite-jdbc vào classpath.", e);
        }
        ensureDirectory();
        DatabaseInitializer.runMigrations();
        initialized = true;
    }

    private static void ensureDirectory() {
        Path path = Path.of(AppConfig.getDatabasePath());
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (Exception e) {
                throw new IllegalStateException("Không thể tạo thư mục lưu database", e);
            }
        }
    }
}
