package dev.sahilpreet.commercecore.merchant.api;

import dev.sahilpreet.commercecore.merchant.Merchant;
import dev.sahilpreet.commercecore.merchant.application.MerchantQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantQueryService merchantQueryService;

    public MerchantController(MerchantQueryService merchantQueryService) {
        this.merchantQueryService = merchantQueryService;
    }

    @GetMapping("/{merchantId}")
    public MerchantResponse getMerchantById(@PathVariable Long merchantId) {
        Merchant merchant = merchantQueryService.getById(merchantId);
        return new MerchantResponse(merchant.id(), merchant.name());
    }
}
