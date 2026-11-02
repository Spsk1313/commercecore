package dev.sahilpreet.commercecore.merchant.application;

public class MerchantNotFoundException extends RuntimeException {
    public MerchantNotFoundException(Long id) {
        super("Merchant not found with id: " + id);
    }
}
