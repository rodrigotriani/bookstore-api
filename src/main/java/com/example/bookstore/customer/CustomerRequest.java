package com.example.bookstore.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to create or update a customer")
public record CustomerRequest(
        @Schema(example = "Ada Lovelace") @NotBlank @Size(max = 255) String name,
        @Schema(description = "Normalized (trimmed, lowercased) before storage; unique",
                example = "ada@example.com") @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "Optional", example = "+44 20 7946 0000") @Size(max = 30) String phone) {

    /** Trims the email before bean validation so surrounding whitespace is tolerated. */
    public CustomerRequest {
        email = email == null ? null : email.trim();
    }
}
