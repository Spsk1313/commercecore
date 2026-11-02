package dev.sahilpreet.commercecore.merchant.application;

import dev.sahilpreet.commercecore.merchant.Merchant;
import dev.sahilpreet.commercecore.merchant.persistence.JdbcMerchantRepository;
import org.springframework.stereotype.Service;

@Service
public class MerchantQueryService {

    private final JdbcMerchantRepository jdbcMerchantRepository;

    public MerchantQueryService(JdbcMerchantRepository jdbcMerchantRepository) {
        this.jdbcMerchantRepository = jdbcMerchantRepository;
    }

    public Merchant getById(Long id) {
        return jdbcMerchantRepository.findById(id).orElseThrow(() -> new MerchantNotFoundException(id));
    }
}
