package dev.sahilpreet.commercecore.identity.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncoderTest {

    private final PasswordHashingConfig passwordHashingConfig = new PasswordHashingConfig();

    private final PasswordEncoder passwordEncoder = passwordHashingConfig.passwordEncoder();

    @Test
    void doesNotReturnPlainText() {
        String encoded = passwordEncoder.encode("myPassword");

        assertNotEquals("myPassword", encoded);
        assertTrue(encoded.contains("m=19456,t=2,p=1"));
    }

    @Test
    void resultingHashIdentifiesArgon2Id() {
        String encoded = passwordEncoder.encode("myPassword");

        assertTrue(encoded.startsWith("$argon2id"));
    }

    @Test
    void matchesReturnsTrueForCorrectPassword() {
        String encoded = passwordEncoder.encode("myPassword");

        assertTrue(passwordEncoder.matches("myPassword", encoded));
    }

    @Test
    void matchesReturnsFalseForWrongPassword() {
        String encoded = passwordEncoder.encode("myPassword");

        assertFalse(passwordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    void returnsDifferentEncodingsForSamePassword() {
        String encoded1 = passwordEncoder.encode("myPassword");
        String encoded2 = passwordEncoder.encode("myPassword");

        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches("myPassword", encoded1));
        assertTrue(passwordEncoder.matches("myPassword", encoded2));
    }
}
