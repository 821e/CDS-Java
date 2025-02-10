package com.cds.automation.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "config.properties";

    static {
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Warning: Could not load config.properties. Using default values.");
        }
    }

    public static int getRetryAttempts() {
        return Integer.parseInt(properties.getProperty("retry.attempts", "3"));
    }

    public static int getRetryDelay() {
        return Integer.parseInt(properties.getProperty("retry.delay", "500"));
    }

    public static int getPageLoadTimeout() {
        return Integer.parseInt(properties.getProperty("page.load.timeout", "30"));
    }

    public static int getElementWaitTimeout() {
        return Integer.parseInt(properties.getProperty("element.wait.timeout", "10"));
    }

    public static String getWebDriverPath() {
        return properties.getProperty("webdriver.chrome.driver", "chromedriver");
    }
}