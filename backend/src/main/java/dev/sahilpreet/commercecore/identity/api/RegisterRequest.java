package dev.sahilpreet.commercecore.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
