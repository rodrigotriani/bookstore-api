package com.example.bookstore.order;

import com.example.bookstore.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Forces a failure after stock was already decremented in memory to prove the transaction rolls back. */
class OrderRollbackTest extends IntegrationTestSupport {

    @MockitoSpyBean
    OrderRepository spiedOrderRepository;

    @Test
    void failureWhilePersistingOrderRollsBackStockChanges() throws Exception {
        long customer = createCustomer("rollback@example.com");
        long b1 = createBook("R-1", "5.00", 10);
        long b2 = createBook("R-2", "7.00", 10);

        Mockito.doThrow(new IllegalStateException("simulated failure"))
                .when(spiedOrderRepository).saveAndFlush(any(BookOrder.class));

        postJson("/api/orders", orderJson(customer, b1, 3, b2, 4)).andExpect(status().isInternalServerError());

        Mockito.reset(spiedOrderRepository);
        assertThat(bookRepository.findById(b1).orElseThrow().getStockQuantity()).isEqualTo(10);
        assertThat(bookRepository.findById(b2).orElseThrow().getStockQuantity()).isEqualTo(10);
        assertThat(orderRepository.count()).isZero();
        assertThat(orderItemRepository.count()).isZero();
    }
}
