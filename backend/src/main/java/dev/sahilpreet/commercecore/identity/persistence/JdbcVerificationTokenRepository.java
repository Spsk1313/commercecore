package dev.sahilpreet.commercecore.identity.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcVerificationTokenRepository {

    private final JdbcClient jdbcClient;

    public JdbcVerificationTokenRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Long> consume(String tokenHash) {
        String updateQuery = """
                UPDATE email_verification_tokens
                SET consumed_at = now()
                WHERE token_hash = :tokenHash
                    AND expires_at > now()
                    AND consumed_at IS NULL
                RETURNING user_id;
                """;

        return jdbcClient.sql(updateQuery)
                .param("tokenHash", tokenHash)
                .query(Long.class)
                .optional();
    }
}
