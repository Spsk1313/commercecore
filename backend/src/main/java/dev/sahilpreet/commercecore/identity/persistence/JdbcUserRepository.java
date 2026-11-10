package dev.sahilpreet.commercecore.identity.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    public Optional<Long> registerUser(String normalizedEmail, String hashedPassword) {
        String createUserQuery = """
                INSERT INTO USERS (email, password_hash)
                VALUES (:normalizedEmail, :passwordHash)
                ON CONFLICT (lower(email)) DO NOTHING
                RETURNING id
                """;

        return jdbcClient.sql(createUserQuery)
                .param("normalizedEmail", normalizedEmail)
                .param("passwordHash", hashedPassword)
                .query(Long.class)
                .optional();
    }
}
