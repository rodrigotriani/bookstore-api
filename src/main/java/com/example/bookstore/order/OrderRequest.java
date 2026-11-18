package com.example.bookstore.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Payload to place an order. A book id may appear at most once.")
public record OrderRequest(
        @Schema(example = "1") @NotNull Long customerId,
        @NotEmpty @Size(max = 100) List<@NotNull @Valid OrderItemRequest> items) {
}
