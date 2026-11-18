package com.example.bookstore.order;

import com.example.bookstore.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

class OrderApiTest extends IntegrationTestSupport {

    private int stockOf(long bookId) {
        return bookRepository.findById(bookId).orElseThrow().getStockQuantity();
    }

    @Test
    void createsOrderWithMultipleBooksComputesTotalsAndDecrementsStock() throws Exception {
        long customer = createCustomer("buyer@example.com");
        long b1 = createBook("O-1", "10.50", 10);
        long b2 = createBook("O-2", "3.25", 5);

        postJson("/api/orders", orderJson(customer, b1, 2, b2, 3))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.customer.id").value(customer))
                .andExpect(jsonPath("$.customer.email").value("buyer@example.com"))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].bookId").value(b1))
                .andExpect(jsonPath("$.items[0].unitPrice").value(10.50))
                .andExpect(jsonPath("$.items[0].subtotal").value(21.00))
                .andExpect(jsonPath("$.items[1].subtotal").value(9.75))
                .andExpect(jsonPath("$.total").value(30.75));

        assertThat(stockOf(b1)).isEqualTo(8);
        assertThat(stockOf(b2)).isEqualTo(2);
    }

    @Test
    void getAndListReturnPersistedOrders() throws Exception {
        long customer = createCustomer("list@example.com");
        long book = createBook("L-1", "5.00", 10);
        String body = postJson("/api/orders", orderJson(customer, book, 1)).andReturn().getResponse()
                .getContentAsString();
        long orderId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5.00))
                .andExpect(jsonPath("$.items[0].bookTitle").value("Title L-1"));
        mockMvc.perform(get("/api/orders")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/orders/9999")).andExpect(status().isNotFound());
    }

    @Test
    void orderingExactRemainingStockSucceedsAndLeavesZero() throws Exception {
        long customer = createCustomer("exact@example.com");
        long book = createBook("E-1", "5.00", 3);
        postJson("/api/orders", orderJson(customer, book, 3)).andExpect(status().isCreated());
        assertThat(stockOf(book)).isZero();
    }

    @Test
    void priceSnapshotSurvivesLaterPriceUpdates() throws Exception {
        long customer = createCustomer("snap@example.com");
        long book = createBook("S-1", "10.00", 10);
        String body = postJson("/api/orders", orderJson(customer, book, 2)).andReturn().getResponse()
                .getContentAsString();
        long orderId = objectMapper.readTree(body).get("id").asLong();

        putJson("/api/books/" + book, bookJson("Title S-1", "S-1", "99.99", 8)).andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(jsonPath("$.items[0].unitPrice").value(10.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(20.00))
                .andExpect(jsonPath("$.total").value(20.00));
    }

    @Test
    void insufficientStockReturns409AndRollsBackEverything() throws Exception {
        long customer = createCustomer("stock@example.com");
        long plenty = createBook("I-1", "5.00", 10);
        long scarce = createBook("I-2", "5.00", 1);

        postJson("/api/orders", orderJson(customer, plenty, 4, scarce, 2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertThat(stockOf(plenty)).isEqualTo(10);
        assertThat(stockOf(scarce)).isEqualTo(1);
        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
    }

    @Test
    void duplicateBookIdsInOneOrderReturn400() throws Exception {
        long customer = createCustomer("dupitems@example.com");
        long book = createBook("D-1", "5.00", 10);

        postJson("/api/orders", orderJson(customer, book, 1, book, 2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Duplicate book id")));

        assertThat(stockOf(book)).isEqualTo(10);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void invalidPayloadsReturn400() throws Exception {
        long customer = createCustomer("invalid@example.com");
        long book = createBook("V-1", "5.00", 10);

        postJson("/api/orders", "{\"customerId\":" + customer + ",\"items\":[]}").andExpect(status().isBadRequest());
        postJson("/api/orders", "{\"items\":[{\"bookId\":" + book + ",\"quantity\":1}]}")
                .andExpect(status().isBadRequest());
        postJson("/api/orders", orderJson(customer, book, 0)).andExpect(status().isBadRequest());
        postJson("/api/orders", orderJson(customer, book, -3)).andExpect(status().isBadRequest());
        postJson("/api/orders", "{\"customerId\":" + customer + ",\"items\":[{\"quantity\":1}]}")
                .andExpect(status().isBadRequest());
        postJson("/api/orders", "{\"customerId\":" + customer + ",\"items\":[null]}")
                .andExpect(status().isBadRequest());
        postJson("/api/orders", "garbage").andExpect(status().isBadRequest());

        assertThat(stockOf(book)).isEqualTo(10);
    }

    @Test
    void unknownCustomerOrBookReturns404() throws Exception {
        long customer = createCustomer("known@example.com");
        long book = createBook("N-1", "5.00", 10);

        postJson("/api/orders", orderJson(9999, book, 1)).andExpect(status().isNotFound());
        postJson("/api/orders", orderJson(customer, book, 1, 9999, 1)).andExpect(status().isNotFound());

        assertThat(stockOf(book)).isEqualTo(10);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void bookAndCustomerWithOrdersCannotBeDeleted() throws Exception {
        long customer = createCustomer("history@example.com");
        long book = createBook("H-1", "5.00", 10);
        postJson("/api/orders", orderJson(customer, book, 1)).andExpect(status().isCreated());

        mockMvc.perform(delete("/api/books/" + book)).andExpect(status().isConflict());
        mockMvc.perform(delete("/api/customers/" + customer)).andExpect(status().isConflict());

        mockMvc.perform(get("/api/books/" + book)).andExpect(status().isOk());
        mockMvc.perform(get("/api/customers/" + customer)).andExpect(status().isOk());
    }

    @Test
    void unreferencedBookAndCustomerCanStillBeDeleted() throws Exception {
        long customer = createCustomer("free@example.com");
        long book = createBook("F-1", "5.00", 10);
        mockMvc.perform(delete("/api/books/" + book)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/customers/" + customer)).andExpect(status().isNoContent());
    }
}
