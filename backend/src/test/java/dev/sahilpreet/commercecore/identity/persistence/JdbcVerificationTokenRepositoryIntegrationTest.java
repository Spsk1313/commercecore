package dev.sahilpreet.commercecore.identity.persistence;

import dev.sahilpreet.commercecore.identity.application.VerificationTokenGenerator;
import dev.sahilpreet.commercecore.identity.application.VerificationTokenHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private Long createUser() {
        String createUserQuery = """
                INSERT INTO users (email, password_hash)
                VALUES (?, ?)
                RETURNING id;
                """;

        Optional<Long> optionalUserId = jdbcClient.sql(createUserQuery)
                .param("alice@example.com")
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
}
