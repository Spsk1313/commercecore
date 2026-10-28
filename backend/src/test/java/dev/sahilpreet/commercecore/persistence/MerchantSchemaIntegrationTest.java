package dev.sahilpreet.commercecore.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class MerchantSchemaIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void v1CreatesMerchantsTable() {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name = 'merchants';
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

        assertEquals(1, count);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", " Merchant", "Merchant ", "Merchant\nName"})
    void rejectsInvalidMerchantName(String name) {
        String sql = "INSERT INTO merchants (name) VALUES (?)";

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(sql, name));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Café Québec",
            "O'Connor's Bakery",
            "Acme & Sons",
            "ਪੰਜਾਬੀ ਸਟੋਰ",
            "A"
    })
    @Transactional
    void acceptsValidMerchantName(String name) {
        String insertSql = """
                INSERT INTO merchants (name)
                VALUES (?)
                RETURNING id;
                """;

        Long id = jdbcTemplate.queryForObject(insertSql, Long.class, name);

        String readSql = """
                SELECT name FROM merchants
                WHERE id = ?;
                """;

        String actual = jdbcTemplate.queryForObject(readSql, String.class, id);

        assertEquals(name, actual);
    }
}
