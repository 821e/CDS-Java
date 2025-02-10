package com.cds.automation.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "automation.log";
    private static LogLevel currentLevel = LogLevel.INFO;

    public enum LogLevel {
        DEBUG(0), INFO(1), ERROR(2);
        
        private final int level;
        
        LogLevel(int level) {
            this.level = level;
        }
        
        public boolean isLoggable(LogLevel other) {
            return this.level <= other.level;
        }
    }

    public static void setLogLevel(LogLevel level) {
        currentLevel = level;
    }

    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    private static void log(LogLevel level, String message) {
        if (!currentLevel.isLoggable(level)) {
            return;
        }

        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] %s: %s%n", timestamp, level, message);

        // Console output
        System.out.print(logMessage);

        // File output
        try {
            Files.write(
                Paths.get(LOG_FILE),
                logMessage.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public static void clearLogs() {
        try {
            Files.deleteIfExists(Paths.get(LOG_FILE));
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
        }
    }
}