package dev.sahilpreet.commercecore.merchant.persistence;

import dev.sahilpreet.commercecore.merchant.Merchant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class JdbcMerchantRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private JdbcMerchantRepository jdbcMerchantRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    @Transactional
    void shouldReturnMerchantWhenMerchantExists() {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String insertionSql = """
                INSERT INTO merchants(name)
                VALUES (:name)
                """;
        jdbcClient.sql(insertionSql)
                .param("name", "John & Sons")
                .update(keyHolder, "id");

        assertNotNull(keyHolder.getKey());

        Long id = keyHolder.getKey().longValue();

        Optional<Merchant> merchant = jdbcMerchantRepository.findById(id);
        assertTrue(merchant.isPresent());
        Merchant merchantEntity = merchant.get();
        assertEquals(id, merchantEntity.id());
        assertEquals("John & Sons", merchantEntity.name());
    }

    @Test
    void shouldReturnEmptyMerchantWhenMerchantDoesNotExist() {
        Optional<Merchant> merchant = jdbcMerchantRepository.findById(-1L);

        assertTrue(merchant.isEmpty());
    }
}
