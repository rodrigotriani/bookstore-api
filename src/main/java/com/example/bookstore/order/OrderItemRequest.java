package com.example.bookstore.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "A line of an order request")
public record OrderItemRequest(
        @Schema(example = "1") @NotNull Long bookId,
        @Schema(example = "2") @Min(1) @Max(10000) int quantity) {
}
