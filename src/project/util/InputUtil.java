package project.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
  Centralized console input utility with validation.
  All controllers use this instead of raw Scanner calls.
 */
public class InputUtil {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // String Input

    /**
      Prompts for a non-blank string.
     */
    public static String readString(String prompt) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = SCANNER.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("   Input cannot be empty. Please try again.");
        }
    }

    /**
      Reads a string that can be empty (optional field).
     */
    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    //  Password Input

    /**
      Reads password (masked via Console if available, falls back to Scanner).
     */
    public static String readPassword(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] pw = console.readPassword(prompt);
            return new String(pw);
        }
        // Fallback for IDEs
        System.out.print(prompt + " [IDE mode - visible]: ");
        return SCANNER.nextLine().trim();
    }

    //  Integer Input

    /**
      Prompts for a valid integer.
     */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println(" Invalid number. Please enter a whole number.");
            }
        }
    }

    /**
      Prompts for an integer within [min, max] inclusive.
     */
    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int val = readInt(prompt);
            if (val >= min && val <= max) return val;
            System.out.printf(" Please enter a number between %d and %d.%n", min, max);
        }
    }

    // BigDecimal / Money Input

    /**
      Prompts for a valid positive BigDecimal (for salary, bonus, deductions).
     */
    public static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            try {
                BigDecimal val = new BigDecimal(line);
                if (val.compareTo(BigDecimal.ZERO) >= 0) return val;
                System.out.println("   Amount cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("   Invalid amount. Please enter a valid number (e.g. 1500.00).");
            }
        }
    }

    //  Date Input

    /**
      Prompts for a date in yyyy-MM-dd format.
     */
    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd): ");
            String line = SCANNER.nextLine().trim();
            try {
                return LocalDate.parse(line, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("   Invalid date format. Use yyyy-MM-dd (e.g. 2025-01-15).");
            }
        }
    }

    //  Yes/No Confirmation

    /**
      Prompts Y/N and returns boolean.
     */
    public static boolean readConfirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String line = SCANNER.nextLine().trim().toUpperCase();
            if (line.equals("Y")) return true;
            if (line.equals("N")) return false;
            System.out.println("   Please enter Y or N.");
        }
    }

    //  Menu Selection

    /**
      Reads a menu choice; returns trimmed string for switch.
     */
    public static String readMenuChoice(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    //  Email Validation

    /**
      Reads and validates a basic email format.
     */
    public static String readEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            if (line.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$")) return line;
            System.out.println("  Invalid email format. Example: user@company.com");
        }
    }

    //  Score Input (0–100)

    /**
      Reads a performance score between 0 and 100.
     */
    public static double readScore(String prompt) {
        while (true) {
            System.out.print(prompt + " (0.00 - 100.00): ");
            String line = SCANNER.nextLine().trim();
            try {
                double score = Double.parseDouble(line);
                if (score >= 0 && score <= 100) return score;
                System.out.println("   Score must be between 0 and 100.");
            } catch (NumberFormatException e) {
                System.out.println("   Invalid score. Enter a decimal number (e.g. 85.5).");
            }
        }
    }

}