package com.cds.automation.core;

import com.cds.automation.ui.WebDriverManager;
import com.cds.automation.ui.WebElementHandler;
import com.cds.automation.util.ExcelReader;
import com.cds.automation.model.DeclarationData;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import java.io.IOException;

public class CustomsDeclarationAutomator {
    private final WebDriver driver;
    private final ExcelReader excelReader;

    public CustomsDeclarationAutomator(String filePath, boolean useHeadless) throws IOException {
        this.excelReader = new ExcelReader(filePath);
        this.driver = WebDriverManager.createDriver(useHeadless);
    }

    public void execute() {
        try {
            String[] credentials = excelReader.readCredentials();
            WebDriverManager.login(driver, credentials[0], credentials[1], credentials[2]);
            processDeclarations();
        } finally {
            cleanup();
        }
    }

    private void processDeclarations() {
        // Implementation of declaration processing
        // This will handle the business logic for processing customs declarations
    }

    private void cleanup() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            try {
                excelReader.close();
            } catch (IOException e) {
                System.err.println("Error closing Excel reader: " + e.getMessage());
            }
        }
    }
}