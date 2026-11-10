package dev.sahilpreet.commercecore.identity.persistence;

import dev.sahilpreet.commercecore.identity.application.VerificationTokenGenerator;
import dev.sahilpreet.commercecore.identity.application.VerificationTokenHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class JdbcVerificationTokenRepositoryIntegrationTest {

    @ServiceConnection
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private JdbcVerificationTokenRepository jdbcVerificationTokenRepository;

    @Test
    @Transactional
    void consumesValidTokenExactlyOnce() {
        Long userId = createUser();

        String hash = generateHash();

        String createEmailVerificationTokenQuery = """
                INSERT INTO email_verification_tokens (user_id, token_hash, expires_at)
                VALUES (?, ?, ?)
                """;

        int noOfRowsAffected = jdbcClient.sql(createEmailVerificationTokenQuery)
                .param(userId)
                .param(hash)
                .param(OffsetDateTime.ofInstant(
                        Instant.now().plus(Duration.ofMinutes(30)),
                        ZoneOffset.UTC
                ))
                .update();

        assertEquals(1, noOfRowsAffected);

        Optional<Long> consumedUserId1 = jdbcVerificationTokenRepository.consume(hash);
        assertTrue(consumedUserId1.isPresent());
        assertEquals(userId, consumedUserId1.get());

        Optional<Long> consumedUserId2 = jdbcVerificationTokenRepository.consume(hash);
        assertTrue(consumedUserId2.isEmpty());

    }

    @Test
    @Transactional
    void doesNotConsumeExpiredToken() {
        Long userId = createUser();

        String hash = generateHash();

        String createEmailVerificationTokenQuery = """
                INSERT INTO email_verification_tokens (
                                    user_id,
                                    token_hash,
                                    created_at,
                                    expires_at
                                )
                                VALUES (
                                    :userId,
                                    :tokenHash,
                                    now() - INTERVAL '2 hours',
                                    now() - INTERVAL '1 hour'
                                );
                """;

        int rowsInserted = jdbcClient.sql(createEmailVerificationTokenQuery)
                .param("userId", userId)
                .param("tokenHash", hash)
                .update();

        assertEquals(1, rowsInserted);

        Optional<Long> result =
                jdbcVerificationTokenRepository.consume(hash);

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    void shouldPersistVerificationTokenForNewUser() {
        Long userId = createUser();
        String tokenHash = generateHash();

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC)
                .plusMinutes(30)
                .truncatedTo(ChronoUnit.MICROS);

        jdbcVerificationTokenRepository.issue(userId, tokenHash, expiresAt);

        var token = jdbcClient.sql("""
                        SELECT token_hash, expires_at, consumed_at
                        FROM email_verification_tokens
                        WHERE user_id = :userId
                        """)
                .param("userId", userId)
                .query((rs, rowNum) -> new VerificationTokenRow(
                        rs.getString("token_hash"),
                        rs.getObject("expires_at", OffsetDateTime.class),
                        rs.getObject("consumed_at", OffsetDateTime.class)
                ))
                .single();

        assertAll(
                () -> assertEquals(tokenHash, token.hash()),
                () -> assertEquals(expiresAt.toInstant(), token.expiresAt().toInstant()),
                () -> assertNull(token.consumedAt())
        );
    }

    @Test
    void shouldRejectDuplicateInitialTokenIssuance() {
        Long userId = createUser();
        try {

            String hashA = generateHash();
            String hashB = generateHash();

            OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC)
                    .plusMinutes(30);

            jdbcVerificationTokenRepository.issue(userId, hashA, expiresAt);

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> jdbcVerificationTokenRepository.issue(userId, hashB, expiresAt)
            );

            List<String> storedHashes = jdbcClient.sql("""
                            SELECT token_hash
                            FROM email_verification_tokens
                            WHERE user_id = :userId
                            """)
                    .param("userId", userId)
                    .query(String.class)
                    .list();

            assertEquals(List.of(hashA), storedHashes);
        } finally {
            cleanupUser(userId);
        }
    }

    private record VerificationTokenRow(
            String hash,
            OffsetDateTime expiresAt,
            OffsetDateTime consumedAt
    ) {
    }

    private Long createUser() {
        String createUserQuery = """
                INSERT INTO users (email, password_hash)
                VALUES (?, ?)
                RETURNING id;
                """;

        Optional<Long> optionalUserId = jdbcClient.sql(createUserQuery)
                .param(generateEmail())
                .param("password_hash")
                .query(Long.class)
                .optional();

        assertTrue(optionalUserId.isPresent());
        return optionalUserId.get();
    }

    private String generateHash() {
        VerificationTokenGenerator tokenGenerator = new VerificationTokenGenerator();
        VerificationTokenHasher tokenHasher = new VerificationTokenHasher();

        String token = tokenGenerator.generate();
        return tokenHasher.hash(token);
    }

    private String generateEmail() {
        return "verify-" + UUID.randomUUID() + "@example.com";
    }

    private void cleanupUser(Long userId) {
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
