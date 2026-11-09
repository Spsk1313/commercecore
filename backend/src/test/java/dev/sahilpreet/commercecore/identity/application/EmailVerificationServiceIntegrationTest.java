
package dev.sahilpreet.commercecore.identity.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class EmailVerificationServiceIntegrationTest {

    @ServiceConnection
    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Test
    void verifiesUserAndConsumesToken() {
        Long userId = createUser();

        try {
            String rawToken = createVerificationToken(userId);

            emailVerificationService.verify(rawToken);

            Boolean emailIsVerified = jdbcClient.sql("""
                            SELECT email_verified_at IS NOT NULL
                            FROM users
                            WHERE id = :userId
                            """)
                    .param("userId", userId)
                    .query(Boolean.class)
                    .single();

            Boolean tokenIsConsumed = jdbcClient.sql("""
                            SELECT consumed_at IS NOT NULL
                            FROM email_verification_tokens
                            WHERE user_id = :userId
                            """)
                    .param("userId", userId)
                    .query(Boolean.class)
                    .single();

            assertTrue(emailIsVerified);
            assertTrue(tokenIsConsumed);

        } finally {
            deleteTestData(userId);
        }
    }

    @Test
    void rollsBackTokenConsumptionWhenUserUpdateFails() {
        Long userId = createUser();

        try {
            String rawToken = createVerificationToken(userId);

            int rowsUpdated = jdbcClient.sql("""
                            UPDATE users
                            SET email_verified_at = now()
                            WHERE id = :userId
                            """)
                    .param("userId", userId)
                    .update();

            assertEquals(1, rowsUpdated);

            assertThrows(
                    IllegalStateException.class,
                    () -> emailVerificationService.verify(rawToken)
            );

            Boolean tokenIsUnconsumed = jdbcClient.sql("""
                            SELECT consumed_at IS NULL
                            FROM email_verification_tokens
                            WHERE user_id = :userId
                            """)
                    .param("userId", userId)
                    .query(Boolean.class)
                    .single();

            assertTrue(tokenIsUnconsumed);

        } finally {
            deleteTestData(userId);
        }
    }

    private Long createUser() {
        return jdbcClient.sql("""
                        INSERT INTO users (email, password_hash)
                        VALUES (:email, :passwordHash)
                        RETURNING id
                        """)
                .param("email", generateEmail())
                .param("passwordHash", "password_hash")
                .query(Long.class)
                .single();
    }

    private String createVerificationToken(Long userId) {
        VerificationTokenGenerator tokenGenerator =
                new VerificationTokenGenerator();

        VerificationTokenHasher tokenHasher =
                new VerificationTokenHasher();

        String rawToken = tokenGenerator.generate();
        String tokenHash = tokenHasher.hash(rawToken);

        int rowsInserted = jdbcClient.sql("""
                        INSERT INTO email_verification_tokens (
                            user_id,
                            token_hash,
                            expires_at
                        )
                        VALUES (:userId, :tokenHash, :expiresAt)
                        """)
                .param("userId", userId)
                .param("tokenHash", tokenHash)
                .param(
                        "expiresAt",
                        OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(30)
                )
                .update();

        assertEquals(1, rowsInserted);

        return rawToken;
    }

    private String generateEmail() {
        return "verify-" + UUID.randomUUID() + "@example.test";
    }

    private void deleteTestData(Long userId) {
        jdbcClient.sql("""
                        DELETE FROM email_verification_tokens
                        WHERE user_id = :userId
                        """)
                .param("userId", userId)
                .update();

        jdbcClient.sql("""
                        DELETE FROM users
                        WHERE id = :userId
                        """)
                .param("userId", userId)
                .update();
    }
}
