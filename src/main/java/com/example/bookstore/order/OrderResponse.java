package com.example.bookstore.order;

import com.example.bookstore.customer.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "A placed order")
public record OrderResponse(
        @Schema(example = "1") Long id,
        @Schema(description = "Customer who placed the order") CustomerSummary customer,
        @Schema(description = "Order creation time (UTC)") Instant createdAt,
        List<OrderItemResponse> items,
        @Schema(description = "Sum of item subtotals", example = "79.80") BigDecimal total) {

    @Schema(description = "Customer summary embedded in an order")
    public record CustomerSummary(
            @Schema(example = "1") Long id,
            @Schema(example = "Ada Lovelace") String name,
            @Schema(example = "ada@example.com") String email) {
    }

    static OrderResponse from(BookOrder order) {
        Customer customer = order.getCustomer();
        return new OrderResponse(order.getId(),
                new CustomerSummary(customer.getId(), customer.getName(), customer.getEmail()),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getTotal());
    }
}
