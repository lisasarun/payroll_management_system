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
    // STRICT parsing: rejects impossible dates like 2026-02-31 (won't Auto-correct to March)
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
                s = SC.nextLine().trim();
            }
            
            // Password length validation: 8-16 characters
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
                // Business rule: base salary must be at least 100 (N/A 0 or very small values)
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

    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd): ");
            String raw = SC.nextLine().trim();
            try {
                return LocalDate.parse(raw, DATE_FMT);
            } catch (DateTimeParseException e) {
                // Covers both Wrong format and impossible calendar dates like 2026-02-31
                System.out.println("  Invalid date. Use yyyy-MM-dd (example: 2026-02-28).");
            }
        }
    }

    /** Read a date and restrict it to a specific year (ect only 2026). */
    public static LocalDate readDateInYear(String prompt, int year) {
        while (true) {
            LocalDate d = readDate(prompt);
            if (d.getYear() == year) return d;
            System.out.println("  Invalid date. Year must be " + year + " (example: " + year + "-02-28).");
        }
    }

    /** Read an end date that must be in the same year and not before the given start date. */
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
            
            // Email length validation: 6-30 characters
            if (s.length() < 6) {
                System.out.println("  Invalid. Email must be at least 6 characters. Try again!");
                continue;
            }
            if (s.length() > 30) {
                System.out.println("  Invalid. Email must not exceed 30 characters. Try again!");
                continue;
            }
            
            // FIX Issue #9: Stricter email validation (max 1 consecutive special char, no ++)
            if (s.matches("^[a-zA-Z0-9]([a-zA-Z0-9._-])*[a-zA-Z0-9]@[a-zA-Z0-9]([a-zA-Z0-9-])*\\.[a-zA-Z]{2,}$")) {
                // Additional check: no consecutive special characters
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

    /** Strong password: 8-16 chars, at least one upper, one lower, one digit, one special character. */
    public static String readStrongPassword(String prompt) {
        while (true) {
            System.out.print(prompt + " (8-16 chars, upper, lower, digit, special): ");
            String s = SC.nextLine().trim();
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

    /** Email: valid format and local part must match the given full name (Ex: Sarunlisa -> sarunlisa@company.com). */
    public static String readEmailMatchingName(String prompt, String fullName) {
        String normalizedName = fullName.toLowerCase().replaceAll("\\s+", "");
        if (normalizedName.isEmpty()) {
            return readEmail(prompt);
        }
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            
            // Email length validation: 6-30 characters
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

    /** Read leave reason: must be 10-40 characters. */
    public static String readLeaveReason(String prompt) {
        while (true) {
            System.out.print(prompt + " (10-40 characters): ");
            String s = SC.nextLine().trim();
            
            if (s.isEmpty()) {
                System.out.println("  Reason cannot be empty.");
                continue;
            }
            
            if (s.length() < 10) {
                System.out.println("  Reason must be at least 10 characters.");
                continue;
            }
            
            if (s.length() > 40) {
                System.out.println("  Reason can be max 40 characters.");
                continue;
            }
            
            return s;
        }
    }
}
