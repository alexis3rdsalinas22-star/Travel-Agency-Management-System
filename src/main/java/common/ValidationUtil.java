package common;

import javax.swing.text.JTextComponent;

public class ValidationUtil {

    private ValidationUtil() {}

    /**
     * Checks if a text component is empty or contains only whitespace.
     */
    public static boolean isEmpty(JTextComponent field) {
        return field == null || field.getText().trim().isEmpty();
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
}
