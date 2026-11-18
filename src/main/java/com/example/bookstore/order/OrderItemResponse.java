package com.example.bookstore.order;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "A line of a placed order")
public record OrderItemResponse(
        @Schema(example = "1") Long bookId,
        @Schema(example = "Clean Code") String bookTitle,
        @Schema(example = "2") int quantity,
        @Schema(description = "Price at purchase time", example = "39.90") BigDecimal unitPrice,
        @Schema(example = "79.80") BigDecimal subtotal) {

    static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getBook().getId(), item.getBook().getTitle(), item.getQuantity(),
                item.getUnitPrice(), item.getSubtotal());
    }
}
