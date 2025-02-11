package com.cds.automation.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.cds.automation.util.Logger;
import com.cds.automation.util.ExceptionHandler;

public class WebDriverManager {
    public static WebDriver createDriver(boolean useHeadless) {
        Logger.info("Creating new WebDriver instance");
        WebDriver driver = ChromeDriverManager.createDriver(useHeadless);
        WebDriverConfig.configureTimeouts(driver);
        return driver;
    }

    public static void login(WebDriver driver, String username, String password, String url) {
        Logger.info("Performing login operation");
        ExceptionHandler.withRetry(() -> {
            driver.get(url);
            
            WebElementWait.waitForPresence(driver, By.id("txtUsername"))
                .sendKeys(username);
            
            WebElementWait.waitForPresence(driver, By.id("txtPassword"))
                .sendKeys(password);
            
            WebElementWait.waitForClickable(driver, By.id("btnLogin"))
                .click();
            
            // Wait for login completion
            WebElementWait.waitForPresence(driver, By.id("ContentPlaceHolder1_txtItemId"));
            Logger.info("Login successful");
        }, "login", 3);
    }
}