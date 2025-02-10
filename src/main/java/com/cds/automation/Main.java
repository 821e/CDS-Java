package com.cds.automation;

import com.cds.automation.core.CustomsDeclarationAutomator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean useHeadless = promptForHeadlessMode(scanner);
            String filePath = promptForFilePath(scanner);
            
            CustomsDeclarationAutomator automator = new CustomsDeclarationAutomator(filePath, useHeadless);
            automator.execute();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static boolean promptForHeadlessMode(Scanner scanner) {
        System.out.println("Select Chrome mode:");
        System.out.println("1. Headless");
        System.out.println("2. Normal");
        return getChoice(scanner, 1, 2) == 1;
    }

    private static String promptForFilePath(Scanner scanner) {
        System.out.print("Please enter the path to your Excel file: ");
        return scanner.nextLine().trim();
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
}