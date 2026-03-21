package project.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.Scanner;

public class InputUtil {

    private static final Scanner SC = new Scanner(System.in);

    private static final DateTimeFormatter DATE_FMT = new DateTimeFormatterBuilder()
            .appendPattern("uuuu-MM-dd")
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  Input cannot be empty.");
        }
    }


    public static String readAdminUsername(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            
            if (s.isEmpty()) {
                System.out.println("  Username cannot be empty.");
                continue;
            }
            
            if (s.length() < 3) {
                System.out.println("  Username must be at least 3 characters.");
                continue;
            }
            
            if (s.length() > 20) {
                System.out.println("  Username must not exceed 20 characters.");
                continue;
            }

            if (!s.matches("^[a-zA-Z]+$")) {
                System.out.println("  Username must contain only letters (no numbers or special characters).");
                continue;
            }
            
            return s;
        }
    }

    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    public static String readPassword(String prompt) {
        while (true) {
            java.io.Console con = System.console();
            String s;
            if (con != null) {
                char[] passwordChars = con.readPassword(prompt + ": ");
                s = new String(passwordChars);
            } else {
                System.out.print(prompt + " [visible]: ");
                s = SC.nextLine();
            }
            

            if (s.length() < 8) {
                System.out.println("  Invalid. Password must be at least 8 characters. Try again!");
                continue;
            }
            if (s.length() > 16) {
                System.out.println("  Invalid. Password must not exceed 16 characters. Try again!");
                continue;
            }
            return s;
        }
    }


    public static String readPasswordConfirmation(String prompt) {
        while (true) {
            java.io.Console con = System.console();
            String s;
            if (con != null) {
                char[] passwordChars = con.readPassword(prompt + ": ");
                s = new String(passwordChars);
            } else {
                System.out.print(prompt + " [visible]: ");
                s = SC.nextLine().trim();
            }
            
            if (s.isEmpty()) {
                System.out.println("  Password cannot be empty.");
                continue;
            }
            return s;
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

                if (v.compareTo(BigDecimal.valueOf(100)) >= 0) return v;
                if (v.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("  Amount must be greater than 0.");
                } else {
                    System.out.println("  Base salary must be at least 100.00.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  Invalid amount.");
            }
        }
    }

    public static BigDecimal readBigDecimalInRange(String prompt, BigDecimal min, BigDecimal max) {
        while (true) {
            System.out.print(prompt);
            String raw = SC.nextLine().trim();
            try {
                BigDecimal v = new BigDecimal(raw);
                if (v.compareTo(min) < 0 || v.compareTo(max) > 0) {
                    System.out.printf("  Amount must be between %s and %s.%n", min.toPlainString(), max.toPlainString());
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  Invalid amount.");
            }
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd): ");
            String raw = SC.nextLine().trim();
            try {
                return LocalDate.parse(raw, DATE_FMT);
            } catch (DateTimeParseException e) {

                System.out.println("  Invalid date. Use yyyy-MM-dd (example: 2026-02-28).");
            }
        }
    }


    public static LocalDate readDateInYear(String prompt, int year) {
        while (true) {
            LocalDate d = readDate(prompt);
            if (d.getYear() == year) return d;
            System.out.println("  Invalid date. Year must be " + year + " (example: " + year + "-02-28).");
        }
    }


    public static LocalDate readEndDateInYearNotBeforeStart(String prompt, int year, LocalDate startDate) {
        while (true) {
            LocalDate end = readDateInYear(prompt, year);
            if (!end.isBefore(startDate)) return end;
            System.out.println("  End date cannot be before start date.");
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
            

            if (s.length() < 6) {
                System.out.println("  Invalid. Email must be at least 6 characters. Try again!");
                continue;
            }
            if (s.length() > 30) {
                System.out.println("  Invalid. Email must not exceed 30 characters. Try again!");
                continue;
            }
            

            if (s.matches("^[a-zA-Z0-9]([a-zA-Z0-9._-])*[a-zA-Z0-9]@[a-zA-Z0-9]([a-zA-Z0-9-])*\\.[a-zA-Z]{2,}$")) {

                if (!s.contains("..") && !s.contains("--") && !s.contains("__") &&
                        !s.contains("++") && !s.contains(".-") && !s.contains("-.")) {
                    return s;
                }
            }
            System.out.println("  Invalid email format. Try again! Example: user@company.com");
        }
    }

    /** Full name: only letters and spaces, 2–50 chars, name-like (no long consonant runs like "asrjri"). */
    public static String readFullName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            if (s.isEmpty()) {
                System.out.println("  Name cannot be empty.");
                continue;
            }
            if (s.length() < 2 || s.length() > 50) {
                System.out.println("  Name must be 2–50 characters.");
                continue;
            }
            if (!s.matches("^[a-zA-Z\\s]+$")) {
                System.out.println("  Name must contain only letters and spaces (e.g. Sarun Lisa).");
                continue;
            }
            if (hasTooManyConsecutiveConsonants(s)) {
                System.out.println("  Name must look like a real name (e.g. sarunlisa), not random letters.");
                continue;
            }
            return s;
        }
    }

    /** Returns true if String has more than 3 consecutive consonants (rejects "asrjri"-style input). */
    private static boolean hasTooManyConsecutiveConsonants(String s) {
        String lower = s.toLowerCase().replaceAll("\\s+", "");
        String consonants = "bcdfghjklmnpqrstvwxyz";
        int count = 0;
        for (int i = 0; i < lower.length(); i++) {
            if (consonants.indexOf(lower.charAt(i)) >= 0) {
                count++;
                if (count > 3) return true;
            } else {
                count = 0;
            }
        }
        return false;
    }


    public static String readStrongPassword(String prompt) {
        while (true) {
            System.out.print(prompt + " (8-16 chars, upper, lower, digit, special): ");
            String s = SC.nextLine();
            if (s.isEmpty()) {
                System.out.println("  Password cannot be empty.");
                continue;
            }
            if (s.length() < 8) {
                System.out.println("  Password must be at least 8 characters.");
                continue;
            }
            if (s.length() > 16) {
                System.out.println("  Password must not exceed 16 characters.");
                continue;
            }
            if (!s.matches(".*[A-Z].*")) {
                System.out.println("  Password must contain at least one uppercase letter.");
                continue;
            }
            if (!s.matches(".*[a-z].*")) {
                System.out.println("  Password must contain at least one lowercase letter.");
                continue;
            }
            if (!s.matches(".*[0-9].*")) {
                System.out.println("  Password must contain at least one digit.");
                continue;
            }
            if (!s.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                System.out.println("  Password must contain at least one special character (!@#$%^&* etc.).");
                continue;
            }
            return s;
        }
    }


    public static String readEmailMatchingName(String prompt, String fullName) {
        String normalizedName = fullName.toLowerCase().replaceAll("\\s+", "");
        if (normalizedName.isEmpty()) {
            return readEmail(prompt);
        }
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            

            if (s.length() < 6) {
                System.out.println("  Email must be at least 6 characters.");
                continue;
            }
            if (s.length() > 30) {
                System.out.println("  Email must not exceed 30 characters.");
                continue;
            }
            
            if (!s.matches("^[a-zA-Z0-9]([a-zA-Z0-9._-])*[a-zA-Z0-9]@[a-zA-Z0-9]([a-zA-Z0-9-])*\\.[a-zA-Z]{2,}$") ||
                    s.contains("..") || s.contains("--") || s.contains("__") ||
                    s.contains("++") || s.contains(".-") || s.contains("-.")) {
                System.out.println("  Invalid email. Example: " + normalizedName + "@company.com");
                continue;
            }
            String localPart = s.split("@")[0].toLowerCase().replaceAll("[._-]", "");
            if (!localPart.contains(normalizedName)) {
                System.out.println("  Email must match the employee name (e.g. " + normalizedName + "@company.com).");
                continue;
            }
            return s;
        }
    }


    public static String validateFullName(String s) {
        s = s.trim();
        if (s.length() < 2 || s.length() > 50) {
            System.out.println("  ✘ Name must be 2–50 characters.");
            return null;
        }
        if (!s.matches("^[a-zA-Z\\s]+$")) {
            System.out.println("  ✘ Name must contain only letters and spaces (e.g. Sarun Lisa).");
            return null;
        }
        if (hasTooManyConsecutiveConsonants(s)) {
            System.out.println("  ✘ Name must look like a real name, not random letters.");
            return null;
        }
        return s;
    }


    public static String validateEmailMatchingName(String s, String fullName) {
        s = s.trim();
        if (!s.matches("^[a-zA-Z0-9]([a-zA-Z0-9._-])*[a-zA-Z0-9]@[a-zA-Z0-9]([a-zA-Z0-9-])*\\.[a-zA-Z]{2,}$") ||
                s.contains("..") || s.contains("--") || s.contains("__") ||
                s.contains("++") || s.contains(".-") || s.contains("-.")) {
            System.out.println("  ✘ Invalid email format. Example: name@company.com");
            return null;
        }
        String normalizedName = fullName.toLowerCase().replaceAll("\\s+", "");
        String localPart = s.split("@")[0].toLowerCase().replaceAll("[._-]", "");
        if (!normalizedName.isEmpty() && !localPart.contains(normalizedName)) {
            System.out.println("  ✘ Email must match the employee name (e.g. " + normalizedName + "@company.com).");
            return null;
        }
        return s;
    }


    public static BigDecimal validateSalary(String s) {
        try {
            BigDecimal v = new BigDecimal(s.trim());
            if (v.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("  ✘ Salary must be greater than 0.");
                return null;
            }
            if (v.compareTo(BigDecimal.valueOf(100)) < 0) {
                System.out.println("  ✘ Base salary must be at least 100.00.");
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            System.out.println("  ✘ Invalid salary amount.");
            return null;
        }
    }

    public static double readScore(String prompt) {
        while (true) {
            System.out.print(prompt + " (0.01-100.00): ");
            String input = SC.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("  Score cannot be empty.");
                continue;
            }
            
            try {
                double v = Double.parseDouble(input);
                if (v > 0 && v <= 100) {
                    return v;
                }
                System.out.println("  Score must be greater than 0 and up to 100.");
            } catch (NumberFormatException e) { 
                System.out.println("  Invalid number format. Please enter a valid score.");
            }
        }
    }


    public static String readLeaveReason(String prompt) {
        while (true) {
            System.out.print(prompt + " (10-100 characters): ");
            String s = SC.nextLine().trim();
            
            if (s.isEmpty()) {
                System.out.println("  Reason cannot be empty.");
                continue;
            }
            
            if (s.length() < 10) {
                System.out.println("  Reason must be at least 10 characters.");
                continue;
            }
            
            if (s.length() > 100) {
                System.out.println("  Reason must not exceed 100 characters.");
                continue;
            }
            
            // Must contain at least one letter
            if (!s.matches(".*[a-zA-Z].*")) {
                System.out.println("  Reason must contain at least one letter.");
                continue;
            }
            
            return s;
        }
    }


    public static String readReviewNote(String prompt) {
        while (true) {
            System.out.print(prompt + " (1-3 sentences): ");
            String s = SC.nextLine().trim();

            if (s.isEmpty()) {
                System.out.println("  Review note cannot be empty.");
                continue;
            }
            if (s.length() < 5) {
                System.out.println("  Review note must be at least 5 characters.");
                continue;
            }
            if (s.length() > 200) {
                System.out.println("  Review note must not exceed 200 characters.");
                continue;
            }
            if (!s.matches(".*[a-zA-Z].*")) {
                System.out.println("  Review note must contain at least one letter.");
                continue;
            }
            if (!s.matches("^[a-zA-Z .,!?()'\\-]+$")) {
                System.out.println("  Review note contains invalid characters.");
                continue;
            }
            int sentenceCount = countSentences(s);
            if (sentenceCount < 1 || sentenceCount > 3) {
                System.out.println("  Review note must contain 1 to 3 sentences.");
                continue;
            }
            return s;
        }
    }

    private static int countSentences(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) return 0;
        // Split by sentence terminators and ignore empty parts.
        String[] parts = normalized.split("[.!?]+");
        int count = 0;
        for (String part : parts) {
            if (!part.trim().isEmpty()) count++;
        }
        return count == 0 ? 1 : count;
    }


    public static String readBonusReason(String prompt) {
        while (true) {
            System.out.print(prompt + " (10-120 characters): ");
            String s = SC.nextLine().trim();

            if (s.isEmpty()) {
                System.out.println("  Reason cannot be empty.");
                continue;
            }
            if (s.length() < 10) {
                System.out.println("  Reason must be at least 10 characters.");
                continue;
            }
            if (s.length() > 120) {
                System.out.println("  Reason must not exceed 120 characters.");
                continue;
            }
            if (!s.matches(".*[a-zA-Z].*")) {
                System.out.println("  Reason must contain at least one letter.");
                continue;
            }
            if (!s.matches("^[a-zA-Z0-9 .,()'\\-_/]+$")) {
                System.out.println("  Reason contains invalid characters.");
                continue;
            }
            return s;
        }
    }


    public static String readChoice(String prompt, String[] choices) {
        System.out.println(prompt);
        for (int i = 0; i < choices.length; i++) {
            System.out.println("    " + (i + 1) + ". " + choices[i]);
        }
        
        while (true) {
            System.out.print("  Select (1-" + choices.length + "): ");
            String input = SC.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("  Selection cannot be empty.");
                continue;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= choices.length) {
                    return choices[choice - 1];
                }
                System.out.println("  Invalid selection. Please choose 1-" + choices.length + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }
}
