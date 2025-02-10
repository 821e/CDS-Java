package com.cds.automation.ui;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WebElementHandler {
    public static void retryOnException(Runnable func) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                func.run();
                return;
            } catch (StaleElementReferenceException | NoSuchElementException | ElementClickInterceptedException e) {
                attempts++;
                if (attempts >= 3) {
                    throw new RuntimeException("Maximum retry attempts reached", e);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

    public static boolean handlePopup(WebDriver driver) {
        try {
            WebElement confirmButton = new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[text()='Confirmar e continuar']")));
            confirmButton.click();
            TimeUnit.MILLISECONDS.sleep(300);
            return true;
        } catch (TimeoutException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static void retryOnStaleElement(Runnable func) {
        retryWithDelay(func, 3, 200);
    }

    public static void retryWithDelay(Runnable func, int maxAttempts, int delayMillis) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxAttempts) {
            try {
                func.run();
                return;
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts >= maxAttempts) {
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(delayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        throw new RuntimeException("Operation failed after " + maxAttempts + " attempts", lastException);
    }

    public static WebElement waitForElement(WebDriver driver, By by, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitForElementToBeClickable(WebDriver driver, By by, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeClickable(by));
    }
}