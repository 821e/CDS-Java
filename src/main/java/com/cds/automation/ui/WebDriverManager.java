package com.cds.automation.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.By;
import java.util.concurrent.TimeUnit;

public class WebDriverManager {
    public static WebDriver createDriver(boolean useHeadless) {
        ChromeOptions options = setupChromeOptions(useHeadless);
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        return driver;
    }

    private static ChromeOptions setupChromeOptions(boolean useHeadless) {
        ChromeOptions options = new ChromeOptions();
        if (useHeadless) {
            options.addArguments("--headless");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return options;
    }

    public static void login(WebDriver driver, String username, String password, String url) {
        driver.get(url);
        WebElementHandler.retryWithDelay(() -> {
            WebElementHandler.waitForElement(driver, By.id("ContentPlaceHolder1_txtUserId"), 30)
                .sendKeys(username);
            driver.findElement(By.id("ContentPlaceHolder1_txtPwd")).sendKeys(password);
            driver.findElement(By.id("ContentPlaceHolder1_btnLogIn")).click();
            WebElementHandler.waitForElement(driver, By.id("ContentPlaceHolder1_txtItemId"), 30);
        }, 3, 5000);
    }
}