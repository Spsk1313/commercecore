package dev.sahilpreet.commercecore.identity.application;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VerificationTokenHasherTest {

    private static VerificationTokenHasher tokenHasher;

    @BeforeAll
    static void setUp() {
        tokenHasher = new VerificationTokenHasher();
    }

    @Test
    void generatesValidHash() {
        String actualHash = tokenHasher.hash("abc");
        String expectedHash = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

        assertEquals(expectedHash, actualHash);
    }

    @Test
    void contains64LowercaseHexCharacters() {
        VerificationTokenGenerator tokenGenerator = new VerificationTokenGenerator();

        String token = tokenGenerator.generate();
        String hash = tokenHasher.hash(token);

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }
}
