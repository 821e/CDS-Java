package com.cds.automation;

import com.cds.automation.core.CustomsDeclarationAutomator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Customs Declaration System Automator");
            System.out.println("==================================");
            
            boolean useHeadless = promptForHeadlessMode(scanner);
            String filePath = promptForFilePath(scanner);
            
            System.out.println("\nStarting automation process...");
            CustomsDeclarationAutomator automator = new CustomsDeclarationAutomator(filePath, useHeadless);
            automator.execute();
            
            System.out.println("\nAutomation completed successfully!");
        } catch (Exception e) {
            System.err.println("\nError during automation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean promptForHeadlessMode(Scanner scanner) {
        while (true) {
            System.out.println("\nSelect Chrome mode:");
            System.out.println("1. Headless (runs in background)");
            System.out.println("2. Normal (visible browser)");
            System.out.print("Enter choice (1-2): ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (choice == 1 || choice == 2) {
                    return choice == 1;
                }
            } else {
                scanner.nextLine(); // Clear invalid input
            }
            System.out.println("Invalid choice. Please enter 1 or 2.");
        }
    }

    private static String promptForFilePath(Scanner scanner) {
        while (true) {
            System.out.print("\nEnter the path to your Excel file: ");
            String filePath = scanner.nextLine().trim();
            
            if (!filePath.isEmpty()) {
                if (!filePath.endsWith(".xlsx") && !filePath.endsWith(".xls")) {
                    System.out.println("Warning: File does not appear to be an Excel file.");
                    System.out.print("Continue anyway? (y/n): ");
                    if (!scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                        continue;
                    }
                }
                return filePath;
            }
            System.out.println("File path cannot be empty. Please try again.");
        }
    }
}