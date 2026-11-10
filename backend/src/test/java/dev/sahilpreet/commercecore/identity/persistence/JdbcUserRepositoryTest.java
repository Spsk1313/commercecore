package dev.sahilpreet.commercecore.identity.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class JdbcUserRepositoryTest {

    @ServiceConnection
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private JdbcUserRepository userRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    @Transactional
    void shouldReturnIdOnSuccessfulInsertion() {
        Optional<Long> registeredUser = userRepository.registerUser(generateEmail(), "passwordHash");
        assertTrue(registeredUser.isPresent());
    }

    @Test
    @Transactional
    void shouldReturnEmptyOnDuplicateInsertion() {
        String normalizedEmail = generateEmail();
        Optional<Long> registeredUser1 = userRepository.registerUser(normalizedEmail, "passwordHashA");
        assertTrue(registeredUser1.isPresent());

        Optional<Long> registeredUser2 = userRepository.registerUser(normalizedEmail.toUpperCase(), "passwordHashB");
        assertTrue(registeredUser2.isEmpty());

        String getHashQuery = """
                SELECT password_hash
                FROM users
                WHERE id = :userId;
                """;

        String hashedPassword = jdbcClient.sql(getHashQuery)
                .param("userId", registeredUser1.get())
                .query(String.class)
                .single();

        assertEquals("passwordHashA", hashedPassword);
    }

    private String generateEmail() {
        return "verify-" + UUID.randomUUID() + "@example.com";
    }
}
