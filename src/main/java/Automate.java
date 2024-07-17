import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Automate {

    public static WebElement waitForElement(WebDriver driver, By by, int timeout) {
        while (true) {
            try {
                return new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOfElementLocated(by));
            } catch (TimeoutException e) {
                System.out.println("Retrying: Timeout waiting for element " + by);
            }
        }
    }

    public static WebElement waitForElementToBeClickable(WebDriver driver, By by, int timeout) {
        while (true) {
            try {
                return new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.elementToBeClickable(by));
            } catch (TimeoutException e) {
                System.out.println("Retrying: Timeout waiting for element " + by + " to be clickable");
            }
        }
    }

    public static void retryOnStaleElement(Runnable func) {
        while (true) {
            try {
                func.run();
                return;
            } catch (StaleElementReferenceException e) {
                System.out.println("Retrying due to stale element reference");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void keepSessionAlive(WebDriver driver) {
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.MINUTES.sleep(2);  // Interact with the page every 2 minutes
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1);");
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -1);");
                    System.out.println("Keeping session alive.");
                } catch (Exception e) {
                    System.out.println("Failed to keep session alive: " + e);
                    break;
                }
            }
        }).start();
    }

    public static String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public static void automate() throws IOException {
        // Load Excel workbook and sheets
        FileInputStream file = new FileInputStream(new File("/Users/rakan/Downloads/CDS.xlsm"));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet otherSheet = workbook.getSheet("Other");
        Sheet dataSheet = workbook.getSheet("Data");

        // Login details initialization
        String u_name = getCellValue(otherSheet.getRow(1).getCell(0));
        String u_pass = getCellValue(otherSheet.getRow(1).getCell(1));
        String url = getCellValue(otherSheet.getRow(1).getCell(2));

        // General order details initialization
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

        // Initialize Script action
        String action = getCellValue(otherSheet.getRow(7).getCell(2));

        // Set up Selenium WebDriver
        WebDriver driver = new ChromeDriver();
        driver.get(url);

        try {
            // Wait for the page to load and login
            waitForElement(driver, By.id("ContentPlaceHolder1_txtUserId"), 10).sendKeys(u_name);
            driver.findElement(By.id("ContentPlaceHolder1_txtPwd")).sendKeys(u_pass);
            driver.findElement(By.id("ContentPlaceHolder1_btnLogIn")).click();

            waitForElement(driver, By.id("ContentPlaceHolder1_txtItemId"), 10);

            // Start the session alive thread
            keepSessionAlive(driver);

            int i = 1; // Excel rows are 0-indexed in POI
            int rowsProcessed = 0;
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

                WebElement element = waitForElementToBeClickable(driver, By.id("ContentPlaceHolder1_txtItemId"), 10);
                element.clear();
                element.sendKeys(reference_id);
                retryOnStaleElement(() -> driver.findElement(By.id("ContentPlaceHolder1_btnOk")).click());

                String curr = getCellValue(row.getCell(19));
                System.out.println("Processing row " + i + " with currency " + curr);

                if (action.equals("ADD")) {
                    insertData(driver, dest_country, dest_post, send_name, send_addr1, send_addr2, send_city, send_state, send_country, send_tele, mail_class, orig_country, nature, franchise, franchise_currency, dataSheet, i, curr);
                } else if (action.equals("UPDATE") || action.equals("DELETE")) {
                    updateData(driver, action, dest_country, dest_post, send_name, send_addr1, send_addr2, send_city, send_state, send_country, send_tele, mail_class, orig_country, nature, franchise, franchise_currency, dataSheet, i, curr);
                }

                i++;
                rowsProcessed++;
            }

            System.out.println(rowsProcessed + " Rows of data " + (action.equals("ADD") ? "added" : action.equals("UPDATE") ? "updated" : "deleted"));

            System.out.println("Last row processed. Please review the browser to confirm the entry.");
            System.in.read();  // Wait for Enter to be pressed

        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e);
        } finally {
            driver.quit();
        }
    }

    public static void retryOnException(Runnable func) {
        while (true) {
            try {
                func.run();
                return;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                System.out.println("Retrying due to: " + e);
                try {
                    TimeUnit.SECONDS.sleep(1);
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
            System.out.println("Set currency value to " + curr + " for row " + i);

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

    public static void main(String[] args) {
        try {
            automate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
