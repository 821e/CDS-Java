package com.cds.automation;

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

public class Automate {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");

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

    public static WebElement waitForElement(WebDriver driver, By by, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement waitForElementToBeClickable(WebDriver driver, By by, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeClickable(by));
    }

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
                processRows(driver, dataSheet, otherSheet, u_name, u_pass);
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

    private static ChromeOptions setupChromeOptions(boolean useHeadless) {
        ChromeOptions options = new ChromeOptions();
        if (useHeadless) {
            options.addArguments("--headless");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        return options;
    }

    private static void performLogin(WebDriver driver, String u_name, String u_pass) {
        retryWithDelay(() -> {
            waitForElement(driver, By.id("ContentPlaceHolder1_txtUserId"), 30).sendKeys(u_name);
            driver.findElement(By.id("ContentPlaceHolder1_txtPwd")).sendKeys(u_pass);
            driver.findElement(By.id("ContentPlaceHolder1_btnLogIn")).click();
            waitForElement(driver, By.id("ContentPlaceHolder1_txtItemId"), 30);
        }, 3, 5000);
    }

    private static void processRows(WebDriver driver, Sheet dataSheet, Sheet otherSheet, String u_name, String u_pass) {
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

        for (int i = 1; i <= dataSheet.getLastRowNum(); i++) {
            final int rowIndex = i;
            Row row = dataSheet.getRow(rowIndex);
            if (row == null || row.getCell(0) == null) {
                break;
            }

            String reference_id = getCellValue(row.getCell(3));
            if (reference_id == null || reference_id.isEmpty()) {
                continue;
            }

            System.out.println("Processing row " + rowIndex);

            try {
                retryWithDelay(() -> {
                    WebElement element = waitForElementToBeClickable(driver, By.id("ContentPlaceHolder1_txtItemId"), 20);
                    element.clear();
                    element.sendKeys(reference_id);
                    retryOnStaleElement(() -> driver.findElement(By.id("ContentPlaceHolder1_btnOk")).click());

                    boolean popupHandled = handlePopup(driver);
                    if (popupHandled) {
                        waitForElement(driver, By.id("ContentPlaceHolder1_txtItemId"), 10);
                    }

                    String curr = getCellValue(row.getCell(19));

                    if (action.equals("ADD")) {
                        insertData(driver, dest_country, dest_post, send_name, send_addr1, send_addr2, send_city, send_state, send_country, send_tele, mail_class, orig_country, nature, franchise, franchise_currency, dataSheet, rowIndex, curr);
                    } else if (action.equals("UPDATE") || action.equals("DELETE")) {
                        updateData(driver, action, dest_country, dest_post, send_name, send_addr1, send_addr2, send_city, send_state, send_country, send_tele, mail_class, orig_country, nature, franchise, franchise_currency, dataSheet, rowIndex, curr);
                    }
                }, 3, 2000);
            } catch (TimeoutException e) {
                System.err.println("Timeout occurred while processing row " + rowIndex + ": " + e.getMessage());
                driver.navigate().refresh();
                performLogin(driver, u_name, u_pass);
            } catch (Exception e) {
                System.err.println("An error occurred while processing row " + rowIndex + ": " + e.getMessage());
            }
        }

        System.out.println("Rows of data processed.");
    }

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

    public static void insertData(WebDriver driver, String dest_country, String dest_post, String send_name, String send_addr1, String send_addr2, String send_city, String send_state, String send_country, String send_tele, String mail_class, String orig_country, String nature, String franchise, String franchise_currency, Sheet dataSheet, int i, String curr) {
        try {
            retryOnException(() -> waitForElement(driver, By.id("ContentPlaceHolder1_txtPartnerCountry"), 10).sendKeys(dest_country));
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_txtPartnerPost")).sendKeys(dest_post));
            retryOnException(() -> new Select(waitForElement(driver, By.id("ContentPlaceHolder1_cbMailClass"), 10)).selectByVisibleText(mail_class));

            WebElement handlingClassElement = waitForElement(driver, By.id("ContentPlaceHolder1_ctl01_ucDeclaration_cbHandlingClass"), 10);
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
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientCountry")).clear());
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientCountry")).sendKeys(reci_country));
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientEmail")).sendKeys(reci_email));
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_txtRecipientTelephone")).sendKeys(reci_tele));
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPNumber_0")).sendKeys(qty));
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPDesc_0")).sendKeys(item_desc));
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPNetWeight_0")).sendKeys(weight));
            retryOnException(() -> driver.findElement(By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPAmount_0")).sendKeys(item_value));

            WebElement currencyElement = waitForElement(driver, By.id("ContentPlaceHolder1_ctl01_ucDeclaration_rptCP_txtCPCurrency_0"), 10);
            currencyElement.clear();
            currencyElement.sendKeys(curr);

            WebElement grossWeightElement = waitForElement(driver, By.id("ContentPlaceHolder1_ctl01_ucDeclaration_ucGrossWeight_txtField"), 10);
            if (grossWeightElement.getAttribute("value").isEmpty()) {
                grossWeightElement.sendKeys(weight);
            }

            WebElement postageElement = waitForElement(driver, By.id("ContentPlaceHolder1_ctl01_ucDeclaration_ucPostage_txtField"), 10);
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

    public static void updateData(WebDriver driver, String action, String dest_country, String dest_post, String send_name, String send_addr1, String send_addr2, String send_city, String send_state, String send_country, String send_tele, String mail_class, String orig_country, String nature, String franchise, String franchise_currency, Sheet dataSheet, int i, String curr) {
        try {
            waitForElement(driver, By.id("ContentPlaceHolder1_grdItems"), 10);
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
}
