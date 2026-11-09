package dev.sahilpreet.commercecore.identity.application;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class VerificationTokenGeneratorTest {

    private static VerificationTokenGenerator tokenGenerator;

    @BeforeAll
    static void setUp() {
        tokenGenerator = new VerificationTokenGenerator();
    }

    @Test
    void generatesTokenWith256Bits() {
        String token = tokenGenerator.generate();

        byte[] bytes = Base64.getUrlDecoder().decode(token);

        assertEquals(32, bytes.length);
    }

    @Test
    void generatesDifferentTokens() {
        String token1 = tokenGenerator.generate();
        String token2 = tokenGenerator.generate();

        assertNotEquals(token1, token2);
    }
}
