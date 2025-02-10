package com.cds.automation.ui;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WebElementHandler {
    public static WebElement waitForElement(WebDriver driver, By by, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitForElementToBeClickable(WebDriver driver, By by, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeClickable(by));
    }

    public static void retryWithDelay(Runnable func, int maxAttempts, int delayMillis) {
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                func.run();
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw e;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(delayMillis);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}