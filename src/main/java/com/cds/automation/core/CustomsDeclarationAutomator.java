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
        try {
            String[] senderInfo = excelReader.readSenderInfo();
            String action = excelReader.getAction();

            for (DeclarationData declaration : excelReader.readDeclarations()) {
                processDeclaration(declaration, senderInfo, action);
            }
            
            System.out.println("All declarations processed successfully.");
        } catch (Exception e) {
            System.err.println("Error processing declarations: " + e.getMessage());
            throw new RuntimeException("Declaration processing failed", e);
        }
    }

    private void processDeclaration(DeclarationData declaration, String[] senderInfo, String action) {
        try {
            WebElementHandler.retryWithDelay(() -> {
                searchDeclaration(declaration.getReferenceId());
                
                switch (action.toUpperCase()) {
                    case "ADD":
                        insertDeclarationData(declaration, senderInfo);
                        break;
                    case "UPDATE":
                        updateDeclarationData(declaration, senderInfo);
                        break;
                    case "DELETE":
                        deleteDeclaration();
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid action: " + action);
                }
            }, 3, 2000);
        } catch (TimeoutException e) {
            handleTimeout(e, declaration.getReferenceId());
        } catch (Exception e) {
            handleError(e, declaration.getReferenceId());
        }
    }

    private void searchDeclaration(String referenceId) {
        WebElement searchField = WebElementHandler.waitForElementToBeClickable(
            driver, By.id("ContentPlaceHolder1_txtItemId"), 20);
        searchField.clear();
        searchField.sendKeys(referenceId);
        
        WebElementHandler.retryOnStaleElement(() -> 
            driver.findElement(By.id("ContentPlaceHolder1_btnOk")).click());

        if (WebElementHandler.handlePopup(driver)) {
            WebElementHandler.waitForElement(driver, 
                By.id("ContentPlaceHolder1_txtItemId"), 10);
        }
    }

    private void insertDeclarationData(DeclarationData declaration, String[] senderInfo) {
        fillSenderInformation(senderInfo);
        fillRecipientInformation(declaration);
        fillItemInformation(declaration);
        submitDeclaration();
    }

    private void updateDeclarationData(DeclarationData declaration, String[] senderInfo) {
        WebElement updateButton = WebElementHandler.waitForElementToBeClickable(
            driver, By.id("ContentPlaceHolder1_btnEdit"), 10);
        updateButton.click();
        
        insertDeclarationData(declaration, senderInfo);
    }

    private void deleteDeclaration() {
        WebElement deleteButton = WebElementHandler.waitForElementToBeClickable(
            driver, By.id("ContentPlaceHolder1_btnDelete"), 10);
        deleteButton.click();
        WebElementHandler.handlePopup(driver);
    }

    private void fillSenderInformation(String[] senderInfo) {
        WebElementHandler.retryOnException(() -> {
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderName", senderInfo[0]);
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderAddressLine1", senderInfo[1]);
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderAddressLine2", senderInfo[2]);
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderCity", senderInfo[3]);
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderState", senderInfo[4]);
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderCountry", senderInfo[5]);
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderTelephone", senderInfo[6]);
        });
    }

    private void fillRecipientInformation(DeclarationData declaration) {
        WebElementHandler.retryOnException(() -> {
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientName", declaration.getRecipientName());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientAddressLine1", declaration.getRecipientAddress1());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientAddressLine2", declaration.getRecipientAddress2());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientZIP", declaration.getRecipientPostCode());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientCity", declaration.getRecipientCity());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientState", declaration.getRecipientState());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientCountry", declaration.getRecipientCountry());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientEmail", declaration.getRecipientEmail());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientTelephone", declaration.getRecipientTelephone());
        });
    }

    private void fillItemInformation(DeclarationData declaration) {
        WebElementHandler.retryOnException(() -> {
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPNumber_0", declaration.getQuantity());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPDesc_0", declaration.getItemDescription());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPNetWeight_0", declaration.getWeight());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPAmount_0", declaration.getItemValue());
            fillFormField("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPCurrency_0", declaration.getCurrency());
        });
    }

    private void fillFormField(String fieldId, String value) {
        WebElement element = driver.findElement(By.id(fieldId));
        element.clear();
        element.sendKeys(value);
    }

    private void submitDeclaration() {
        WebElementHandler.retryOnException(() -> {
            WebElement submitButton = WebElementHandler.waitForElementToBeClickable(
                driver, By.id("ContentPlaceHolder1_btnSubmit"), 10);
            submitButton.click();
            WebElementHandler.handlePopup(driver);
        });
    }

    private void handleTimeout(TimeoutException e, String referenceId) {
        System.err.println("Timeout occurred while processing declaration " + referenceId + ": " + e.getMessage());
        driver.navigate().refresh();
        String[] credentials = excelReader.readCredentials();
        WebDriverManager.login(driver, credentials[0], credentials[1], credentials[2]);
    }

    private void handleError(Exception e, String referenceId) {
        System.err.println("Error processing declaration " + referenceId + ": " + e.getMessage());
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