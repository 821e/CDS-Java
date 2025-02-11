package com.cds.automation.ui;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import com.cds.automation.util.Logger;
import java.time.Duration;
import java.util.function.Function;

public class WebElementWait {
    private static final int DEFAULT_TIMEOUT = 10;
    private static final int POLLING_INTERVAL = 500;

    public static WebElement waitForPresence(WebDriver driver, By locator) {
        return waitForPresence(driver, locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForPresence(WebDriver driver, By locator, int timeoutSeconds) {
        Logger.debug("Waiting for element presence: " + locator);
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            Logger.error("Element not present after " + timeoutSeconds + " seconds: " + locator);
            throw e;
        }
    }

    public static WebElement waitForClickable(WebDriver driver, By locator) {
        return waitForClickable(driver, locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForClickable(WebDriver driver, By locator, int timeoutSeconds) {
        Logger.debug("Waiting for element to be clickable: " + locator);
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            Logger.error("Element not clickable after " + timeoutSeconds + " seconds: " + locator);
            throw e;
        }
    }

    public static boolean waitForInvisibility(WebDriver driver, By locator) {
        return waitForInvisibility(driver, locator, DEFAULT_TIMEOUT);
    }

    public static boolean waitForInvisibility(WebDriver driver, By locator, int timeoutSeconds) {
        Logger.debug("Waiting for element invisibility: " + locator);
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            Logger.error("Element still visible after " + timeoutSeconds + " seconds: " + locator);
            throw e;
        }
    }

    public static <T> T waitFor(WebDriver driver, Function<WebDriver, T> condition) {
        return waitFor(driver, condition, DEFAULT_TIMEOUT);
    }

    public static <T> T waitFor(WebDriver driver, Function<WebDriver, T> condition, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))
            .until(condition);
    }
}