package com.cds.automation.ui;

import org.openqa.selenium.WebDriver;
import java.time.Duration;
import com.cds.automation.util.Logger;

public class WebDriverConfig {
    private static final int PAGE_LOAD_TIMEOUT = 30;
    private static final int IMPLICIT_WAIT = 10;
    private static final int SCRIPT_TIMEOUT = 20;

    public static void configureTimeouts(WebDriver driver) {
        Logger.info("Configuring WebDriver timeouts...");
        try {
            driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT))
                .implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT))
                .scriptTimeout(Duration.ofSeconds(SCRIPT_TIMEOUT));
            
            driver.manage().window().maximize();
            Logger.info("WebDriver timeouts configured successfully");
        } catch (Exception e) {
            Logger.error("Failed to configure WebDriver timeouts: " + e.getMessage());
            throw e;
        }
    }

    public static void cleanupDriver(WebDriver driver) {
        if (driver != null) {
            Logger.info("Cleaning up WebDriver session...");
            try {
                driver.manage().deleteAllCookies();
                driver.quit();
                Logger.info("WebDriver cleanup completed");
            } catch (Exception e) {
                Logger.error("Error during WebDriver cleanup: " + e.getMessage());
            }
        }
    }
}