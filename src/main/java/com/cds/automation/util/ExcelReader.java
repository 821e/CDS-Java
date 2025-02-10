package com.cds.automation.util;

import com.cds.automation.model.DeclarationData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader implements AutoCloseable {
    private final Workbook workbook;
    private final Sheet dataSheet;
    private final Sheet otherSheet;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");

    public ExcelReader(String filePath) throws IOException {
        FileInputStream file = new FileInputStream(new File(filePath));
        this.workbook = new XSSFWorkbook(file);
        this.dataSheet = workbook.getSheet("Data");
        this.otherSheet = workbook.getSheet("Other");
    }

    public String[] readCredentials() {
        return new String[]{
            getCellValue(otherSheet.getRow(1).getCell(0)), // username
            getCellValue(otherSheet.getRow(1).getCell(1)), // password
            getCellValue(otherSheet.getRow(1).getCell(2))  // url
        };
    }

    public String[] readSenderInfo() {
        Row senderRow = otherSheet.getRow(4);
        return new String[]{
            getCellValue(senderRow.getCell(2)),  // sender name
            getCellValue(senderRow.getCell(3)),  // address line 1
            getCellValue(senderRow.getCell(4)),  // address line 2
            getCellValue(senderRow.getCell(5)),  // city
            getCellValue(senderRow.getCell(6)),  // state
            getCellValue(senderRow.getCell(8)),  // country
            getCellValue(senderRow.getCell(7))   // telephone
        };
    }

    public String getAction() {
        return getCellValue(otherSheet.getRow(7).getCell(2));
    }

    public List<DeclarationData> readDeclarations() {
        List<DeclarationData> declarations = new ArrayList<>();
        int firstRow = 1; // Assuming first row is header
        int lastRow = dataSheet.getLastRowNum();

        for (int i = firstRow; i <= lastRow; i++) {
            Row row = dataSheet.getRow(i);
            if (row == null) continue;

            DeclarationData declaration = createDeclarationFromRow(row);
            if (declaration != null) {
                declarations.add(declaration);
            }
        }
        return declarations;
    }

    private DeclarationData createDeclarationFromRow(Row row) {
        try {
            return new DeclarationData(
                getCellValue(row.getCell(0)),   // referenceId
                getCellValue(row.getCell(6)),   // recipientAddress1
                getCellValue(row.getCell(7)),   // recipientAddress2
                getCellValue(row.getCell(5)),   // recipientCity
                getCellValue(row.getCell(8)),   // recipientState
                getCellValue(row.getCell(9)),   // recipientPostCode
                getCellValue(row.getCell(10)),  // recipientCountry
                getCellValue(row.getCell(11)),  // recipientName
                getCellValue(row.getCell(12)),  // recipientTelephone
                getCellValue(row.getCell(13)),  // recipientEmail
                getCellValue(row.getCell(16)),  // itemDescription
                getCellValue(row.getCell(17)),  // quantity
                getCellValue(row.getCell(20)),  // weight
                getCellValue(row.getCell(21)),  // itemValue
                getCellValue(row.getCell(22))   // currency
            );
        } catch (Exception e) {
            System.err.println("Error reading row " + row.getRowNum() + ": " + e.getMessage());
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
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

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }
}