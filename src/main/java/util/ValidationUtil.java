package util;

/**
 * ValidationUtil - Server-side validation helpers.
 */
public class ValidationUtil {

    /** Returns true if string is non-null and non-empty after trimming. */
    public static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** Simple email format check. */
    public static boolean isValidEmail(String email) {
        return isNotBlank(email) && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    /** Returns true if value can be parsed as a positive double. */
    public static boolean isPositiveDouble(String value) {
        if (!isNotBlank(value)) return false;
        try {
            return Double.parseDouble(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
