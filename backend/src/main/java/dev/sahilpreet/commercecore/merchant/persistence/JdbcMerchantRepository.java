package dev.sahilpreet.commercecore.merchant.persistence;

import dev.sahilpreet.commercecore.merchant.Merchant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcMerchantRepository {

    private final JdbcClient jdbcClient;

    public JdbcMerchantRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Merchant> findById(Long id) {
        String findQuery = "SELECT id, name FROM merchants WHERE id = :id";
        return jdbcClient.sql(findQuery)
                .param("id", id)
                .query(Merchant.class)
                .optional();
    }

}
