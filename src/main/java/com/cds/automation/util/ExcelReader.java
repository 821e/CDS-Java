package com.cds.automation.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.text.DecimalFormat;

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