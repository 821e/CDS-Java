# Comprehensive Documentation for the `Automate` Class

## Overview

The `Automate` class is designed to automate web interactions using Selenium WebDriver and handle Excel files using Apache POI. The application reads data from an Excel file, navigates through a website to perform specific actions such as form submissions, and manages web elements dynamically. This guide provides a detailed explanation of the class and its components, ensuring you understand every aspect of its functionality.

## Prerequisites

Before using the `Automate` class, ensure you have the following:

1. **Java Development Kit (JDK)**: Install the latest version of the JDK.
2. **Maven**: Install Maven for dependency management.
3. **ChromeDriver**: Download the ChromeDriver executable and ensure it's in your system's PATH.
4. **Excel File**: Prepare an Excel file with the required structure.

### Maven Dependencies

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>4.1.2</version>
    </dependency>
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>3.141.59</version>
    </dependency>
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-chrome-driver</artifactId>
        <version>3.141.59</version>
    </dependency>
</dependencies>
```

## Class Structure

### Package Declaration

```java
package com.example;
```

### Imports

```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
```

### Class Declaration

```java
public class Automate {
```

### Constants

```java
private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");
```

### Main Method

The main method is the entry point of the application.

```java
public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Select Chrome mode:");
    System.out.println("1. Headless");
    System.out.println("2. Normal");
    int choice = getChoice(scanner, 1, 2);
    boolean useHeadless = (choice == 1);

    System.out.print("Please enter the path to your Excel file: ");
    String filePath = scanner.nextLine().trim();

    try {
        automate(filePath, useHeadless);
    } catch (IOException e) {
        System.err.println("Error reading the Excel file: " + e.getMessage());
    }
}
```

#### Description

1. **User Input**: Prompts the user to select the Chrome mode (headless or normal) and enter the path to the Excel file.
2. **Automation Call**: Calls the `automate` method with the provided file path and Chrome mode.

### getChoice Method

This method ensures the user provides a valid choice within a specified range.

```java
private static int getChoice(Scanner scanner, int min, int max) {
    int choice;
    while (true) {
        System.out.print("Enter choice (" + min + "-" + max + "): ");
        if (scanner.hasNextInt()) {
            choice = scanner.nextInt();
            if (choice >= min && choice <= max) {
                break;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        } else {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next(); // Clear invalid input
        }
    }
    scanner.nextLine(); // Consume newline left-over
    return choice;
}
```

#### Description

1. **Input Validation**: Continuously prompts the user until a valid choice is entered.
2. **Range Enforcement**: Ensures the choice is within the specified range.

### waitForElement Method

Waits for an element to be visible on the web page.

```java
public static WebElement waitForElement(WebDriver driver, By by, int timeout) {
    return new WebDriverWait(driver, Duration.ofSeconds(timeout))
            .until(ExpectedConditions.visibilityOfElementLocated(by));
}
```

#### Description

1. **WebDriver**: The Selenium WebDriver instance.
2. **Locator**: The `By` locator for the web element.
3. **Timeout**: Maximum wait time in seconds.

#### Returns

- A visible WebElement.

### waitForElementToBeClickable Method

Waits for an element to be clickable on the web page.

```java
public static WebElement waitForElementToBeClickable(WebDriver driver, By by, int timeout) {
    return new WebDriverWait(driver, Duration.ofSeconds(timeout))
            .until(ExpectedConditions.elementToBeClickable(by));
}
```

#### Description

1. **WebDriver**: The Selenium WebDriver instance.
2. **Locator**: The `By` locator for the web element.
3. **Timeout**: Maximum wait time in seconds.

#### Returns

- A clickable WebElement.

### retryOnStaleElement Method

Retries an operation up to three times if a `StaleElementReferenceException` is encountered.

```java
public static void retryOnStaleElement(Runnable func) {
    int attempts = 0;
    while (attempts < 3) {
        try {
            func.run();
            return;
        } catch (StaleElementReferenceException e) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            attempts++;
        }
    }
}
```

#### Description

1. **Runnable**: A functional interface containing the operation to retry.

### getCellValue Method

Retrieves the value of an Excel cell as a String.

```java
public static String getCellValue(Cell cell) {
    switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue();
        case NUMERIC:
            return decimalFormat.format(cell.getNumericCellValue());
        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
        case FORMULA:
            return cell.getCellFormula();
        default:
            return "";
    }
}
```

#### Description

1. **Cell**: The Excel cell.

#### Returns

- The cell value as a String.

### handlePopup Method

Handles popup dialogs on the web page by clicking the "Confirmar e continuar" button if present.

```java
public static boolean handlePopup(WebDriver driver) {
    try {
        WebElement confirmButton;
        while ((confirmButton = new WebDriverWait(driver, Duration.ofSeconds(2))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Confirmar e continuar']")))) != null) {
            confirmButton.click();
            TimeUnit.MILLISECONDS.sleep(300);
        }
        return true;
    } catch (TimeoutException e) {
        // No more popups to handle
    } catch (Exception e) {
        System.out.println("Failed to click on 'Confirmar e Continuar' button: " + e);
    }
    return false;
}
```

#### Description

1. **WebDriver**: The Selenium WebDriver instance.

#### Returns

- `true` if the popup was handled, `false` otherwise.

### automate Method

The main automation method. Reads data from the Excel file, sets up the Chrome browser, and performs web interactions based on the data.

```java
public static void automate(String filePath, boolean useHeadless) throws IOException {
    FileInputStream file = new FileInputStream(new File(filePath));
    Workbook workbook = null;
    try {
        workbook = new XSSFWorkbook(file);
        Sheet otherSheet = workbook.getSheet("Other");
        Sheet dataSheet = workbook.getSheet("Data");

        String u_name = getCellValue(otherSheet.getRow(1).getCell(0));
        String u_pass = getCellValue(otherSheet.getRow(1).getCell(1));
        String url = getCellValue(otherSheet.getRow(1).getCell(2));

        ChromeOptions options = setupChromeOptions(useHeadless);
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        driver.get(url);

        try {
            performLogin(driver, u_name, u_pass);
            processRows(driver, dataSheet, otherSheet);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e);
        } finally {
            driver.quit();
        }
    } finally {
        if (workbook != null) {
            workbook.close();
        }
        file.close();
    }
}
```

#### Description

1. **filePath**: Path to the Excel file.
2. **useHeadless**: Boolean indicating whether to use headless mode for Chrome.

#### Throws

- `IOException` if there is an issue reading the Excel file.

### setupChromeOptions Method

Sets up Chrome options based on the specified mode (headless or normal).

```java
private static ChromeOptions setupChromeOptions(boolean useHeadless) {
    ChromeOptions options = new ChromeOptions();
    if (useHeadless) {
        options.addArguments("--headless

");
    }
    options.addArguments("--disable-gpu");
    options.addArguments("--window-size=1920,1080");
    return options;
}
```

#### Description

1. **useHeadless**: Boolean indicating whether to use headless mode.

#### Returns

- A configured ChromeOptions instance.

### performLogin Method

Performs login on the website using the provided username and password.

```java
private static void performLogin(WebDriver driver, String u_name, String u_pass) {
    waitForElement(driver, By.id("ContentPlaceHolder1_txtUserId"), 10).sendKeys(u_name);
    driver.findElement(By.id("ContentPlaceHolder1_txtPwd")).sendKeys(u_pass);
    driver.findElement(By.id("ContentPlaceHolder1_btnLogIn")).click();
    waitForElement(driver, By.id("ContentPlaceHolder1_txtItemId"), 20);
}
```

#### Description

1. **WebDriver**: The Selenium WebDriver instance.
2. **u_name**: Username for login.
3. **u_pass**: Password for login.

### processRows Method

Processes rows of data from the Excel sheet and performs corresponding web actions.

```java
private static void processRows(WebDriver driver, Sheet dataSheet, Sheet otherSheet) {
    int i = 1;
    int rowsProcessed = 0;

    String dest_country = getCellValue(otherSheet.getRow(4).getCell(0));
    String dest_post = getCellValue(otherSheet.getRow(4).getCell(1));
    String send_name = getCellValue(otherSheet.getRow(4).getCell(2));
    String send_addr1 = getCellValue(otherSheet.getRow(4).getCell(3));
    String send_addr2 = getCellValue(otherSheet.getRow(4).getCell(4));
    String send_city = getCellValue(otherSheet.getRow(4).getCell(5));
    String send_state = getCellValue(otherSheet.getRow(4).getCell(6));
    String send_tele = getCellValue(otherSheet.getRow(4).getCell(7));
    String send_country = getCellValue(otherSheet.getRow(4).getCell(8));
    String mail_class = getCellValue(otherSheet.getRow(4).getCell(9));
    String orig_country = getCellValue(otherSheet.getRow(4).getCell(10));
    String nature = getCellValue(otherSheet.getRow(4).getCell(11));
    String franchise = getCellValue(otherSheet.getRow(4).getCell(12));
    String franchise_currency = getCellValue(otherSheet.getRow(4).getCell(13));

    String action = getCellValue(otherSheet.getRow(7).getCell(2));

    while (true) {
        Row row = dataSheet.getRow(i);
        if (row == null || row.getCell(0) == null) {
            break;
        }

        String reference_id = getCellValue(row.getCell(3));
        if (reference_id == null || reference_id.isEmpty()) {
            i++;
            continue;
        }

        System.out.println("Processing row " + i);

        WebElement element = waitForElementToBeClickable(driver, By.id("ContentPlaceHolder1_txtItemId"), 10);
        element.clear();
        element.sendKeys(reference_id);
        retryOnStaleElement(() -> driver.findElement(By.id("ContentPlaceHolder1_btnOk")).click());

        boolean popupHandled = handlePopup(driver);
        if (popupHandled) {
            waitForElement(driver, By.id("ContentPlaceHolder1_txtItemId"), 2);
        }

        String curr = getCellValue(row.getCell(19));

        if (action.equals("ADD")) {
            insertData(driver, dest_country, dest_post, send_name, send_addr1, send_addr2, send_city, send_state, send_country, send_tele, mail_class, orig_country, nature, franchise, franchise_currency, dataSheet, i, curr);
        } else if (action.equals("UPDATE") || action.equals("DELETE")) {
            updateData(driver, action, dest_country, dest_post, send_name, send_addr1, send_addr2, send_city, send_state, send_country, send_tele, mail_class, orig_country, nature, franchise, franchise_currency, dataSheet, i, curr);
        }

        i++;
        rowsProcessed++;
    }

    System.out.println(rowsProcessed + " Rows of data processed.");
}
```

#### Description

1. **WebDriver**: The Selenium WebDriver instance.
2. **dataSheet**: Sheet containing the main data.
3. **otherSheet**: Sheet containing additional information.

### retryOnException Method

Retries an operation up to three times if certain exceptions are encountered.

```java
public static void retryOnException(Runnable func) {
    int attempts = 0;
    while (attempts < 3) {
        try {
            func.run();
            return;
        } catch (StaleElementReferenceException | NoSuchElementException | ElementClickInterceptedException e) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            attempts++;
        } catch (Exception e) {
            System.out.println("An error occurred: " + e);
            return;
        }
    }
}
```

#### Description

1. **Runnable**: A functional interface containing the operation to retry.

### insertData Method

Inserts data into the web form.

```java
public static void insertData(WebDriver driver, String dest_country, String dest_post, String send_name, String send_addr1, String send_addr2, String send_city, String send_state, String send_country, String send_tele, String mail_class, String orig_country, String nature, String franchise, String franchise_currency, Sheet dataSheet, int i, String curr) {
    try {
        retryOnException(() -> waitForElement(driver, By.id("ContentPlaceHolder1_txtPartnerCountry"), 5).sendKeys(dest_country));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_txtPartnerPost")).sendKeys(dest_post));
        retryOnException(() -> new Select(waitForElement(driver, By.id("ContentPlaceHolder1_cbMailClass"), 5)).selectByVisibleText(mail_class));

        WebElement handlingClassElement = waitForElement(driver, By.id("ContentPlaceHolder1_ctl01_ucDeclaration_cbHandlingClass"), 5);
        new Select(handlingClassElement).selectByVisibleText("N (Normal)");

        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderName")).sendKeys(send_name));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderAddressLine1")).sendKeys(send_addr1));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderAddressLine2")).sendKeys(send_addr2));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderCity")).sendKeys(send_city));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderState")).sendKeys(send_state));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderCountry")).clear());
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderCountry")).sendKeys(send_country));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtSenderTelephone")).sendKeys(send_tele));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtNatureType")).sendKeys(nature));

        Row row = dataSheet.getRow(i);
        String reci_addr1 = getCellValue(row.getCell(5));
        String reci_addr2 = getCellValue(row.getCell(6));
        String reci_city = getCellValue(row.getCell(7));
        String reci_state = getCellValue(row.getCell(8));
        String reci_post = getCellValue(row.getCell(9));
        String reci_country = getCellValue(row.getCell(10));
        String reci_name = getCellValue(row.getCell(11));
        String reci_tele = getCellValue(row.getCell(12));
        String reci_email = getCellValue(row.getCell(13));
        String item_desc = getCellValue(row.getCell(16));
        String qty = getCellValue(row.getCell(17));
        String weight = getCellValue(row.getCell(20));
        String item_value = getCellValue(row.getCell(21));

        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientName")).sendKeys(reci_name));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientAddressLine1")).sendKeys(reci_addr1));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientAddressLine2")).sendKeys(reci_addr2));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientZIP")).sendKeys(reci_post));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientCity")).sendKeys(reci_city));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientState")).sendKeys(reci_state));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1

_ctl01_ucDeclaration_txtRecipientCountry")).clear());
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientCountry")).sendKeys(reci_country));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientEmail")).sendKeys(reci_email));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientTelephone")).sendKeys(reci_tele));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPNumber_0")).sendKeys(qty));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPDesc_0")).sendKeys(item_desc));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPNetWeight_0")).sendKeys(weight));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPAmount_0")).sendKeys(item_value));

        WebElement currencyElement = waitForElement(driver, By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPCurrency_0"), 5);
        currencyElement.clear();
        currencyElement.sendKeys(curr);

        WebElement grossWeightElement = waitForElement(driver, By.id("ContentPlaceHolder1_ctl01_ucDeclaration_ucGrossWeight_txtField"), 5);
        if (grossWeightElement.getAttribute("value").isEmpty()) {
            grossWeightElement.sendKeys(weight);
        }

        WebElement postageElement = waitForElement(driver, By.id("ContentPlaceHolder1_ctl01_ucDeclaration_ucPostage_txtField"), 5);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", postageElement);
        postageElement.clear();
        postageElement.sendKeys(franchise);

        WebElement postageCurrencyElement = driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtPostageCurrency"));
        postageCurrencyElement.clear();
        postageCurrencyElement.sendKeys(franchise_currency);

        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_cbNewAction")).sendKeys("2 (Armazenar final (transferência automática))"));
        retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_btnStore")).click());

        handlePopup(driver);

    } catch (Exception e) {
        System.out.println("An error occurred in insertData: " + e);
    }
}
```

#### Description

1. **WebDriver**: The Selenium WebDriver instance.
2. **dest_country**: Destination country.
3. **dest_post**: Destination post.
4. **send_name**: Sender's name.
5. **send_addr1**: Sender's address line 1.
6. **send_addr2**: Sender's address line 2.
7. **send_city**: Sender's city.
8. **send_state**: Sender's state.
9. **send_country**: Sender's country.
10. **send_tele**: Sender's telephone number.
11. **mail_class**: Mail class.
12. **orig_country**: Origin country.
13. **nature**: Nature of the item.
14. **franchise**: Franchise information.
15. **franchise_currency**: Franchise currency.
16. **dataSheet**: Sheet containing the main data.
17. **i**: Row index.
18. **curr**: Currency.

### updateData Method

Updates data on the web form based on the action specified (UPDATE or DELETE).

```java
public static void updateData(WebDriver driver, String action, String dest_country, String dest_post, String send_name, String send_addr1, String send_addr2, String send_city, String send_state, String send_country, String send_tele, String mail_class, String orig_country, String nature, String franchise, String franchise_currency, Sheet dataSheet, int i, String curr) {
    try {
        waitForElement(driver, By.id("ContentPlaceHolder1_grdItems"), 5);
        WebElement updateID = driver.findElement(By.id("ContentPlaceHolder1_grdItems")).findElement(By.tagName("a"));
        retryOnStaleElement(updateID::click);

        if (action.equals("UPDATE")) {
            insertData(driver, dest_country, dest_post, send_name, send_addr1, send_addr2, send_city, send_state, send_country, send_tele, mail_class, orig_country, nature, franchise, franchise_currency, dataSheet, i, curr);
        } else if (action.equals("DELETE")) {
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_cbNewAction")).sendKeys("8 (Eliminar)"));
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_btnStore")).click());
        }
    } catch (Exception e) {
        System.out.println("An error occurred in updateData: " + e);
    }
}
```

#### Description

1. **WebDriver**: The Selenium WebDriver instance.
2. **action**: Action to perform (UPDATE or DELETE).
3. **dest_country**: Destination country.
4. **dest_post**: Destination post.
5. **send_name**: Sender's name.
6. **send_addr1**: Sender's address line 1.
7. **send_addr2**: Sender's address line 2.
8. **send_city**: Sender's city.
9. **send_state**: Sender's state.
10. **send_country**: Sender's country.
11. **send_tele**: Sender's telephone number.
12. **mail_class**: Mail class.
13. **orig_country**: Origin country.
14. **nature**: Nature of the item.
15. **franchise**: Franchise information.
16. **franchise_currency**: Franchise currency.
17. **dataSheet**: Sheet containing the main data.
18. **i**: Row index.
19. **curr**: Currency.

## Excel File Structure

The Excel file should have two sheets: "Other" and "Data". Below is the expected structure of each sheet:

### "Other" Sheet

| Row | Column 0        | Column 1      | Column 2      | ... |
|-----|-----------------|---------------|---------------|-----|
| 1   | Username        | Password      | URL           |     |
| 4   | Dest Country    | Dest Post     | Send Name     | ... |
| 7   | Action          |               | ADD/UPDATE/DELETE |     |

### "Data" Sheet

| Row | Column 0 | Column 1 | Column 2 | Column 3   | ... |
|-----|----------|----------|----------|------------|-----|
| 1   | ...      | ...      | ...      | Reference ID | ... |

## Usage Instructions

1. **Prepare the Environment**:
   - Ensure Java and Maven are installed on your machine.
   - Add the necessary dependencies to your `pom.xml`.
   - Download the ChromeDriver executable and ensure it's in your system's PATH.

2. **Prepare the Excel File**:
   - Create an Excel file with the required structure.
   - Ensure the "Other" sheet contains login details and additional configuration.
   - Ensure the "Data" sheet contains the main data to be processed.

3. **Run the Application**:
   - Compile and run the `Automate` class.
   - Follow the prompts to select the Chrome mode and provide the path to your Excel file.

4. **Monitor the Output**:
   - The application will process each row of the "Data" sheet and perform the corresponding actions on the web page.
   - Any errors or issues encountered will be printed to the console for debugging.

By following these instructions, you can effectively use the `Automate` class to automate web interactions and handle Excel data processing.