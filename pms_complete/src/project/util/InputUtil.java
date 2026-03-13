package project.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputUtil {

    private static final Scanner SC = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  Input cannot be empty.");
        }
    }

    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    public static String readPassword(String prompt) {
        java.io.Console con = System.console();
        if (con != null) return new String(con.readPassword(prompt + ": "));
        System.out.print(prompt + " [visible]: ");
        return SC.nextLine().trim();
    }


    public static String readPasswordWithPolicy(String prompt) {
        // Regex: at least 8 chars, at least one letter and one digit, no spaces
        final String pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@#$%^&+=!?.]{8,}$";
        while (true) {
            String pwd = readPassword(prompt);
            if (pwd.isEmpty()) {
                System.out.println("  Password cannot be empty.");
                continue;
            }
            if (pwd.matches(pattern)) {
                return pwd;
            }
            System.out.println("  Password must be at least 8 characters, contain letters and digits,");
            System.out.println("  and may only use these symbols: @ # $ % ^ & + = ! ? . (no spaces).");
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(SC.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Enter a whole number."); }
        }
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int v = readInt(prompt);
            if (v >= min && v <= max) return v;
            System.out.printf("  Enter a number between %d and %d.%n", min, max);
        }
    }

    public static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                BigDecimal v = new BigDecimal(SC.nextLine().trim());
                if (v.compareTo(BigDecimal.ZERO) > 0) {
                    return v;
                }
                System.out.println("  Amount must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid amount.");
            }
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd): ");
            try { return LocalDate.parse(SC.nextLine().trim(), DATE_FMT); }
            catch (DateTimeParseException e) { System.out.println("  Use format yyyy-MM-dd."); }
        }
    }

    public static boolean readConfirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String s = SC.nextLine().trim().toUpperCase();
            if (s.equals("Y")) return true;
            if (s.equals("N")) return false;
            System.out.println("  Enter Y or N.");
        }
    }

    public static String readMenuChoice(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    public static String readEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            // FIX Issue #9: Stricter email validation (max 1 consecutive special char, no ++)
            if (s.matches("^[a-zA-Z0-9]([a-zA-Z0-9._-])*[a-zA-Z0-9]@[a-zA-Z0-9]([a-zA-Z0-9-])*\\.[a-zA-Z]{2,}$")) {
                // Additional check: no consecutive special characters
                if (!s.contains("..") && !s.contains("--") && !s.contains("__") &&
                    !s.contains("++") && !s.contains(".-") && !s.contains("-.")) {
                    return s;
                }
            }
            System.out.println("  Invalid email. Example: user@company.com");
        }
    }

    public static double readScore(String prompt) {
        while (true) {
            System.out.print(prompt + " (0.00-100.00): ");
            try {
                double v = Double.parseDouble(SC.nextLine().trim());
                if (v >= 0 && v <= 100) return v;
                System.out.println("  Score must be 0–100.");
            } catch (NumberFormatException e) { System.out.println("  Invalid score."); }
        }
    }
}
