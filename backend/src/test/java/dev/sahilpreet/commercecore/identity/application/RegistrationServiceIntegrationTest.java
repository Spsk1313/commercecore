
package dev.sahilpreet.commercecore.identity.application;

import dev.sahilpreet.commercecore.identity.persistence.JdbcVerificationTokenRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
class RegistrationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterNewUserAndIssueVerificationToken() {
        String normalizedEmail = uniqueEmail();
        String email = "  " + normalizedEmail.toUpperCase(Locale.ROOT) + "  ";
        String password = "SecurePassword123!";

        try {
            Instant before = Instant.now();

            registrationService.register(email, password);

            Instant after = Instant.now();

            List<UserRow> users = findUsers(normalizedEmail);
            assertEquals(1, users.size());

            UserRow user = users.getFirst();

            assertEquals(normalizedEmail, user.email());
            assertTrue(user.passwordHash().startsWith("$argon2id$"));
            assertTrue(passwordEncoder.matches(
                    password, user.passwordHash()
            ));

            List<TokenRow> tokens = findTokens(normalizedEmail);
            assertEquals(1, tokens.size());

            TokenRow token = tokens.getFirst();

            assertEquals(user.id(), token.userId());
            assertNotNull(token.tokenHash());
            assertTrue(token.tokenHash().matches("[0-9a-f]{64}"));

            assertNotNull(token.expiresAt());

            Instant expiration = token.expiresAt().toInstant();

            Instant earliest = before.plus(30, ChronoUnit.MINUTES)
                    .minusSeconds(1);

            Instant latest = after.plus(30, ChronoUnit.MINUTES)
                    .plusSeconds(1);

            assertFalse(expiration.isBefore(earliest));
            assertFalse(expiration.isAfter(latest));

            assertNull(token.consumedAt());

        } finally {
            cleanup(normalizedEmail);
        }
    }

    @Test
    void shouldNotModifyExistingUserOnDuplicateRegistration() {
        String normalizedEmail = uniqueEmail();
        String originalPassword = "OriginalPassword123!";
        String differentPassword = "DifferentPassword456!";

        try {
            registrationService.register(
                    normalizedEmail,
                    originalPassword
            );

            List<UserRow> originalUsers = findUsers(normalizedEmail);
            List<TokenRow> originalTokens = findTokens(normalizedEmail);

            assertEquals(1, originalUsers.size());
            assertEquals(1, originalTokens.size());

            UserRow originalUser = originalUsers.getFirst();
            TokenRow originalToken = originalTokens.getFirst();

            String originalPasswordHash = originalUser.passwordHash();
            String originalTokenHash = originalToken.tokenHash();

            registrationService.register(
                    normalizedEmail.toUpperCase(Locale.ROOT),
                    differentPassword
            );

            List<UserRow> usersAfter = findUsers(normalizedEmail);
            List<TokenRow> tokensAfter = findTokens(normalizedEmail);

            assertEquals(1, usersAfter.size());
            assertEquals(1, tokensAfter.size());

            UserRow userAfter = usersAfter.getFirst();
            TokenRow tokenAfter = tokensAfter.getFirst();

            assertAll(
                    () -> assertEquals(
                            originalUser.id(), userAfter.id()
                    ),
                    () -> assertEquals(
                            originalPasswordHash, userAfter.passwordHash()
                    ),
                    () -> assertEquals(
                            originalTokenHash, tokenAfter.tokenHash()
                    ),
                    () -> assertEquals(
                            originalToken, tokenAfter
                    )
            );

        } finally {
            cleanup(normalizedEmail);
        }
    }

    // -------------------------------------------------
    // Rollback test: isolated mocked repository context
    // -------------------------------------------------

    @Nested
    class RollbackTests {

        @MockitoBean
        private JdbcVerificationTokenRepository tokenRepository;

        @Autowired
        private RegistrationService rollbackRegistrationService;

        @Autowired
        private JdbcClient rollbackJdbcClient;

        @Test
        void shouldRollbackUserWhenVerificationTokenIssuanceFails() {
            String normalizedEmail = uniqueEmail();
            String password = "SecurePassword123!";

            doThrow(new DataIntegrityViolationException(
                    "Simulated verification token insertion failure"
            )).when(tokenRepository).issue(
                    anyLong(),
                    anyString(),
                    any(OffsetDateTime.class)
            );

            try {
                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> rollbackRegistrationService.register(
                                normalizedEmail, password
                        )
                );

                // Verify the service reached the failing dependency.
                verify(tokenRepository).issue(
                        anyLong(),
                        anyString(),
                        any(OffsetDateTime.class)
                );

                // Query PostgreSQL outside the service transaction.
                Long userCount = rollbackJdbcClient.sql("""
                                SELECT COUNT(*)
                                FROM users
                                WHERE LOWER(email) = :email
                                """)
                        .param("email", normalizedEmail)
                        .query(Long.class)
                        .single();

                assertEquals(
                        0L,
                        userCount,
                        "User insertion must roll back when token issuance fails"
                );

            } finally {
                // Also cleans up a persisted user if the mutation
                // experiment removes @Transactional from the service.
                cleanup(normalizedEmail);
            }
        }
    }

    // -------------------------------------------------
    // Test helpers
    // -------------------------------------------------

    private String uniqueEmail() {
        return "new.user-" + UUID.randomUUID() + "@example.com";
    }

    private List<UserRow> findUsers(String normalizedEmail) {
        return jdbcClient.sql("""
                        SELECT id, email, password_hash
                        FROM users
                        WHERE LOWER(email) = :email
                        """)
                .param("email", normalizedEmail)
                .query((rs, rowNum) -> new UserRow(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                ))
                .list();
    }

    private List<TokenRow> findTokens(String normalizedEmail) {
        return jdbcClient.sql("""
                        SELECT t.user_id,
                               t.token_hash,
                               t.expires_at,
                               t.consumed_at
                        FROM email_verification_tokens t
                        JOIN users u ON u.id = t.user_id
                        WHERE LOWER(u.email) = :email
                        """)
                .param("email", normalizedEmail)
                .query((rs, rowNum) -> new TokenRow(
                        rs.getLong("user_id"),
                        rs.getString("token_hash"),
                        rs.getObject("expires_at", OffsetDateTime.class),
                        rs.getObject("consumed_at")
                ))
                .list();
    }

    private void cleanup(String normalizedEmail) {
        jdbcClient.sql("""
                        DELETE FROM email_verification_tokens
                        WHERE user_id IN (
                            SELECT id FROM users
                            WHERE LOWER(email) = :email
                        )
                        """)
                .param("email", normalizedEmail)
                .update();

        jdbcClient.sql("""
                        DELETE FROM users
                        WHERE LOWER(email) = :email
                        """)
                .param("email", normalizedEmail)
                .update();
    }

    private record UserRow(
            Long id,
            String email,
            String passwordHash
    ) {
    }

    private record TokenRow(
            Long userId,
            String tokenHash,
            OffsetDateTime expiresAt,
            Object consumedAt
    ) {
    }
}
