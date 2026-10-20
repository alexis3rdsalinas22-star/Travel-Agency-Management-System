package common;

import javax.swing.text.JTextComponent;

public class ValidationUtil {

    private ValidationUtil() {}

    /**
     * Checks if a text component is empty or contains only whitespace.
     */
    public static boolean isEmpty(JTextComponent field) {
        return field == null || field.getText() == null || field.getText().trim().isEmpty();
    }

    /**
     * Checks if any of the provided text components are empty.
     */
    public static boolean isAnyEmpty(JTextComponent... fields) {
        for (JTextComponent f : fields) {
            if (isEmpty(f)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a string into an Integer, returning null if invalid or not positive (> 0).
     */
    public static Integer parsePositiveInt(String text) {
        if (text == null) {
            return null;
        }
        try {
            int val = Integer.parseInt(text.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a string into a Double, returning null if invalid or not positive (> 0).
     */
    public static Double parsePositiveDouble(String text) {
        if (text == null) {
            return null;
        }
        try {
            double val = Double.parseDouble(text.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a search keyword into an integer ID, returning -1 if non-numeric.
     */
    public static int parseSearchId(String keyword) {
        if (keyword == null) {
            return -1;
        }
        try {
            return Integer.parseInt(keyword.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Validates a contact/phone number.
     * Accepts:
     * - Philippine mobile numbers: +63 9XXXXXXXXX, +639XXXXXXXXX, 09XXXXXXXXX, or 9XXXXXXXXX (10-12 digits)
     * - Philippine landlines with area code: (02) 8XXX XXXX, 02 8XXXXXXX
     * - Standard international phone numbers: +[country code][number] (8 to 15 digits)
     * Rejects empty strings, letters, symbols (except leading +, spaces, hyphens, parentheses), and invalid lengths.
     */
    public static boolean isValidPhoneNumber(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.equals("+63 9") || trimmed.equals("+63") || trimmed.equals("+")) {
            return false;
        }

        // Must not contain letters or disallowed characters
        // Allowed characters: digits, spaces, hyphens, parentheses, and an optional single '+' at the beginning
        if (!trimmed.matches("^\\+?[0-9\\s\\-\\(\\)]{7,25}$")) {
            return false;
        }

        // Strip formatting characters (spaces, hyphens, parentheses)
        String digitsOnly = trimmed.replaceAll("[\\s\\-\\(\\)]", "");

        // If it starts with '+', verify the rest are all digits and within standard ITU length (8 to 15 digits)
        if (digitsOnly.startsWith("+")) {
            String num = digitsOnly.substring(1);
            if (!num.matches("\\d{8,15}")) {
                return false;
            }
            // If it's a Philippine +63 number, it must be valid:
            // Mobile: +639XXXXXXXXX (12 digits total after +: 63 + 9 + 9 digits)
            // Landline: +63(area)(number) e.g. +6328XXXXXXX (10-11 digits)
            if (num.startsWith("639")) {
                return num.length() == 12;
            }
            if (num.startsWith("63")) {
                return num.length() >= 10 && num.length() <= 12;
            }
            return true;
        }

        // Philippine numbers without leading '+':
        // Starts with 639 (12 digits)
        if (digitsOnly.startsWith("639")) {
            return digitsOnly.length() == 12;
        }
        // Starts with 09 (11 digits)
        if (digitsOnly.startsWith("09")) {
            return digitsOnly.length() == 11;
        }
        // Starts with 9 (10 digits)
        if (digitsOnly.startsWith("9") && digitsOnly.length() == 10) {
            return true;
        }
        // Philippine landline (e.g. 0281234567: 10 digits)
        if (digitsOnly.startsWith("0") && digitsOnly.length() == 10) {
            return true;
        }

        // International without '+' (8 to 15 digits)
        return digitsOnly.matches("\\d{8,15}");
    }

    /**
     * Validates that a person's name contains only letters, spaces, hyphens, periods, and apostrophes.
     */
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.matches("^[A-Za-z\\s\\.\\-\\']{2,50}$");
    }

    /**
     * Validates that an email address follows standard user@domain.tld format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String trimmed = email.trim();
        return trimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validates a transaction/payment reference based on the selected payment method.
     * Returns true if valid, false otherwise.
     */
    public static boolean isValidPaymentReference(String method, String reference) {
        return getPaymentReferenceError(method, reference) == null;
    }

    /**
     * Returns a human-readable error message explaining why a payment reference
     * is invalid for the specified payment method, or null if it is valid.
     */
    public static String getPaymentReferenceError(String method, String reference) {
        if (method == null || "Cash".equalsIgnoreCase(method)) {
            return null; // Reference is not required for cash
        }

        if (reference == null || reference.trim().isEmpty()) {
            return "A transaction reference / confirmation code is required for " + method + ".";
        }

        String trimmed = reference.trim();

        if ("GCash".equalsIgnoreCase(method)) {
            String digitsOnly = trimmed.replaceAll("[\\s\\-]", "");
            if (!trimmed.matches("^[0-9\\s\\-]+$")) {
                return "GCash reference code must contain only numbers.";
            }
            if (digitsOnly.length() != 13) {
                return "GCash reference code must be exactly 13 digits (e.g., 1001234567890).";
            }
            return null;
        }

        if ("Bank Transfer".equalsIgnoreCase(method)) {
            if (trimmed.length() > 45) {
                return "Bank Transfer reference must be 45 characters or less.";
            }
            if (!trimmed.matches("^[A-Za-z0-9\\-\\s]+$")) {
                return "Bank Transfer reference must contain only letters, numbers, and hyphens.";
            }
            String cleaned = trimmed.replaceAll("[\\s\\-]", "");
            if (cleaned.length() < 6) {
                return "Bank Transfer reference must be at least 6 alphanumeric characters (e.g., BT-98432178).";
            }
            return null;
        }

        if ("Credit Card".equalsIgnoreCase(method)) {
            if (trimmed.length() > 20) {
                return "Credit Card authorization code must be 20 characters or less.";
            }
            if (!trimmed.matches("^[A-Za-z0-9\\-\\s]+$")) {
                return "Credit Card authorization code must contain only letters, numbers, and hyphens.";
            }
            String cleaned = trimmed.replaceAll("[\\s\\-]", "");
            if (cleaned.length() < 6) {
                return "Credit Card authorization code must be at least 6 alphanumeric characters (e.g., 123456 or AUTH12).";
            }
            return null;
        }

        if ("Debit Card".equalsIgnoreCase(method)) {
            if (trimmed.length() > 20) {
                return "Debit Card authorization code must be 20 characters or less.";
            }
            if (!trimmed.matches("^[A-Za-z0-9\\-\\s]+$")) {
                return "Debit Card authorization code must contain only letters, numbers, and hyphens.";
            }
            String cleaned = trimmed.replaceAll("[\\s\\-]", "");
            if (cleaned.length() < 6) {
                return "Debit Card authorization code must be at least 6 alphanumeric characters (e.g., 123456 or DB1234).";
            }
            return null;
        }

        // Generic fallback
        if (trimmed.length() < 4) {
            return "Transaction reference must be at least 4 characters.";
        }
        if (trimmed.length() > 45) {
            return "Transaction reference must be 45 characters or less.";
        }
        return null;
    }

    /**
     * Normalizes a payment reference string for database persistence.
     * For GCash, strips whitespace and hyphens to leave the pure 13-digit code.
     * For other methods, returns the trimmed string.
     */
    public static String normalizePaymentReference(String method, String reference) {
        if (reference == null) {
            return "";
        }
        String trimmed = reference.trim();
        if ("GCash".equalsIgnoreCase(method)) {
            return trimmed.replaceAll("[\\s\\-]", "");
        }
        return trimmed;
    }
}
