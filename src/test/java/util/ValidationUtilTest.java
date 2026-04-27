package util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil. Demonstrates parameterized tests for
 * exercising many inputs without repeating boilerplate.
 */
class ValidationUtilTest {

    // ── isNotBlank ────────────────────────────────────────────────────────

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void isNotBlank_returnsFalseForBlankInput(String input) {
        assertFalse(ValidationUtil.isNotBlank(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "hello", "  hello  "})
    void isNotBlank_returnsTrueForNonBlankInput(String input) {
        assertTrue(ValidationUtil.isNotBlank(input));
    }

    // ── isValidEmail ──────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@sub.domain.co",
            "a_b-c@x-y.io"
    })
    void isValidEmail_acceptsWellFormedAddresses(String email) {
        assertTrue(ValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "no-at-sign.com",
            "user@",
            "@host.com",
            "user@host",
            "user @host.com"
    })
    void isValidEmail_rejectsMalformedAddresses(String email) {
        assertFalse(ValidationUtil.isValidEmail(email));
    }

    @Test
    void isValidEmail_rejectsNull() {
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    // ── isPositiveDouble ──────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"1", "1.5", "0.0001", "999999.99"})
    void isPositiveDouble_acceptsPositiveNumbers(String value) {
        assertTrue(ValidationUtil.isPositiveDouble(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01", "abc", ""})
    void isPositiveDouble_rejectsZeroNegativeOrNonNumeric(String value) {
        assertFalse(ValidationUtil.isPositiveDouble(value));
    }

    @Test
    void isPositiveDouble_rejectsNull() {
        assertFalse(ValidationUtil.isPositiveDouble(null));
    }
}
