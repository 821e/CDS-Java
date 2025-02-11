package com.cds.automation.util;

import org.openqa.selenium.WebDriverException;
import java.io.IOException;

public class ExceptionHandler {
    public static void handleException(Exception e, String operation) {
        String errorMessage = String.format("Error during %s: %s", operation, e.getMessage());
        Logger.error(errorMessage);

        if (e instanceof WebDriverException) {
            Logger.error("WebDriver error - possible connection or element interaction issue");
        } else if (e instanceof IOException) {
            Logger.error("I/O error - check file permissions and paths");
        }

        // Log stack trace for debugging
        Logger.debug("Stack trace:");
        for (StackTraceElement element : e.getStackTrace()) {
            Logger.debug(element.toString());
        }
    }

    public static <T> T withRetry(ThrowingSupplier<T> operation, String operationName, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                return operation.get();
            } catch (Exception e) {
                attempts++;
                Logger.error(String.format("Attempt %d/%d failed for %s", attempts, maxRetries, operationName));
                if (attempts >= maxRetries) {
                    throw new RuntimeException("Maximum retry attempts reached for " + operationName, e);
                }
                try {
                    Thread.sleep(1000 * attempts); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        throw new RuntimeException("Unexpected error in retry logic");
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}