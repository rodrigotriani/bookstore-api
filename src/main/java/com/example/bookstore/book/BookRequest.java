package com.example.bookstore.book;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Payload to create or update a book")
public record BookRequest(
        @Schema(example = "Clean Code") @NotBlank @Size(max = 255) String title,
        @Schema(example = "Robert C. Martin") @NotBlank @Size(max = 255) String author,
        @Schema(example = "9780132350884") @NotBlank @Size(max = 20) String isbn,
        @Schema(example = "39.90") @NotNull @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Schema(example = "10") @Min(0) int stockQuantity) {
}
