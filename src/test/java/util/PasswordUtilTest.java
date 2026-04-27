package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil — pure logic, no external dependencies.
 */
class PasswordUtilTest {

    @Test
    void hash_producesSha256HexOf64Chars() {
        String hashed = PasswordUtil.hash("password123");
        assertEquals(64, hashed.length());
        assertTrue(hashed.matches("[0-9a-f]+"), "hash must be lowercase hex");
    }

    @Test
    void hash_isDeterministic() {
        assertEquals(PasswordUtil.hash("secret"), PasswordUtil.hash("secret"));
    }

    @Test
    void hash_differsForDifferentInputs() {
        assertNotEquals(PasswordUtil.hash("alpha"), PasswordUtil.hash("beta"));
    }

    @Test
    void hash_matchesKnownSha256Vector() {
        // SHA-256("admin") — same value MySQL's SHA2('admin', 256) produces
        String expected = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918";
        assertEquals(expected, PasswordUtil.hash("admin"));
    }

    @Test
    void verify_returnsTrueForCorrectPassword() {
        String stored = PasswordUtil.hash("mypassword");
        assertTrue(PasswordUtil.verify("mypassword", stored));
    }

    @Test
    void verify_returnsFalseForWrongPassword() {
        String stored = PasswordUtil.hash("mypassword");
        assertFalse(PasswordUtil.verify("wrongpassword", stored));
    }
}
