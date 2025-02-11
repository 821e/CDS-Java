package com.cds.automation.ui;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import com.cds.automation.util.Logger;

public class ChromeDriverManager {
    public static ChromeDriver createDriver(boolean headless) {
        Logger.info("Initializing Chrome WebDriver...");
        ChromeOptions options = new ChromeOptions();
        
        if (headless) {
            Logger.info("Running in headless mode");
            options.addArguments("--headless=new");
        }

        options.addArguments("--start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");

        try {
            ChromeDriver driver = new ChromeDriver(options);
            WebDriverConfig.configureTimeouts(driver);
            return driver;
        } catch (Exception e) {
            Logger.error("Failed to initialize Chrome WebDriver: " + e.getMessage());
            throw new RuntimeException("Chrome WebDriver initialization failed", e);
        }
    }
}