package dev.sahilpreet.commercecore.identity.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository {

    private final JdbcClient jdbcClient;

    public JdbcUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public int markEmailVerified(Long userId) {
        String markVerifiedQuery = """
                UPDATE users
                SET email_verified_at = now()
                WHERE id = :userId
                AND email_verified_at IS NULL
                """;

        return jdbcClient.sql(markVerifiedQuery)
                .param("userId", userId)
                .update();
    }
}
