package dev.sahilpreet.commercecore.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
public class UserMembershipSchemaIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private JdbcClient client;

    @Test
    @Transactional
    void rejectsDuplicateEmailIgnoringCase() {
        createUser("Alice@example.com");

        assertThrows(DataIntegrityViolationException.class,
                () -> createUser("alice@example.com"));
    }

    @Test
    @Transactional
    void rejectsDuplicateMerchantMembership() {
        String createOwnerQuery = """
                INSERT INTO merchant_memberships (user_id, merchant_id, role)
                VALUES (?, ?, ?);
                """;

        Long userId = createUser("alice@example.com");
        Long merchantId = createMerchant("Alice Bakery");

        client.sql(createOwnerQuery)
                .param(userId)
                .param(merchantId)
                .param("OWNER")
                .update();

        assertThrows(DataIntegrityViolationException.class,
                () -> client.sql(createOwnerQuery)
                        .param(userId)
                        .param(merchantId)
                        .param("STAFF")
                        .update());
    }

    @Test
    @Transactional
    void rejectsInvalidMembershipRole() {
        Long userId = createUser("alice@example.com");
        Long merchantId = createMerchant("Alice Bakery");

        String createMembershipQuery = """
                INSERT INTO merchant_memberships (user_id, merchant_id, role)
                VALUES (?, ?, ?);
                """;

        assertThrows(DataIntegrityViolationException.class,
                () -> client.sql(createMembershipQuery)
                        .param(userId)
                        .param(merchantId)
                        .param("ADMIN")
                        .update());
    }

    @Test
    @Transactional
    void rejectsMissingMembershipRole() {
        Long userId = createUser("alice@example.com");
        Long merchantId = createMerchant("Alice Bakery");

        String createMembershipQuery = """
                INSERT INTO merchant_memberships (user_id, merchant_id, role)
                VALUES (?, ?, ?);
                """;

        assertThrows(DataIntegrityViolationException.class,
                () -> client.sql(createMembershipQuery)
                        .param(userId)
                        .param(merchantId)
                        .param(null)
                        .update());
    }

    @Test
    @Transactional
    void rejectsMembershipForNonExistentUser() {
        Long merchantId = createMerchant("Singh cafe");

        String createMembershipQuery = """
                INSERT INTO merchant_memberships (user_id, merchant_id, role)
                VALUES (?, ?, ?);
                """;

        assertThrows(DataIntegrityViolationException.class,
                () -> client.sql(createMembershipQuery)
                        .param(-1L)
                        .param(merchantId)
                        .param("OWNER")
                        .update());
    }

    @Test
    @Transactional
    void rejectsMembershipForNonexistentMerchant() {
        Long userId = createUser("singh@example.com");

        String createMembershipQuery = """
                INSERT INTO merchant_memberships (user_id, merchant_id, role)
                VALUES (?, ?, ?);
                """;

        assertThrows(DataIntegrityViolationException.class,
                () -> client.sql(createMembershipQuery)
                        .param(userId)
                        .param(-1L)
                        .param("OWNER")
                        .update());
    }

    private Long createUser(String email) {
        String createUserQuery = """
                INSERT INTO users (email, password_hash)
                VALUES (?, ?);
                """;

        KeyHolder userKeyHolder = new GeneratedKeyHolder();

        client.sql(createUserQuery)
                .param(email)
                .param("password_hash")
                .update(userKeyHolder, "id");

        assertNotNull(userKeyHolder.getKey());

        return userKeyHolder.getKey().longValue();
    }

    private Long createMerchant(String name) {
        String createMerchantQuery = """
                INSERT INTO merchants (name)
                VALUES (?);
                """;

        KeyHolder merchantKeyHolder = new GeneratedKeyHolder();

        client.sql(createMerchantQuery)
                .param(name)
                .update(merchantKeyHolder, "id");

        assertNotNull(merchantKeyHolder.getKey());

        return merchantKeyHolder.getKey().longValue();
    }

}
