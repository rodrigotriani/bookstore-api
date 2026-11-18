package com.example.bookstore.customer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A registered customer")
public record CustomerResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Ada Lovelace") String name,
        @Schema(example = "ada@example.com") String email,
        @Schema(example = "+44 20 7946 0000") String phone) {

    static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone());
    }
}
