package dev.sahilpreet.commercecore.merchant.application;

import dev.sahilpreet.commercecore.merchant.Merchant;
import dev.sahilpreet.commercecore.merchant.persistence.JdbcMerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MerchantQueryServiceTest {

    @Mock
    private JdbcMerchantRepository jdbcMerchantRepository;

    private MerchantQueryService merchantQueryService;

    @BeforeEach
    public void setUp() {
        merchantQueryService = new MerchantQueryService(jdbcMerchantRepository);
    }

    @Test
    void shouldReturnWhenMerchantExists() {
        given(jdbcMerchantRepository.findById(42L))
                .willReturn(Optional.of(new Merchant(42L, "John & Sons")));

        Merchant merchant = merchantQueryService.getById(42L);

        assertNotNull(merchant);
        assertEquals(42L, merchant.id());
        assertEquals("John & Sons", merchant.name());
    }

    @Test
    void shouldThrowWhenMerchantDoesNotExist() {
        given(jdbcMerchantRepository.findById(42L)).willReturn(Optional.empty());

        MerchantNotFoundException ex = assertThrows(MerchantNotFoundException.class,
                () -> merchantQueryService.getById(42L));

        assertEquals("Merchant not found with id: 42", ex.getMessage());
    }
}
