package com.example.bookstore.customer;

import com.example.bookstore.common.error.ConflictException;
import com.example.bookstore.order.OrderRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    OrderRepository orderRepository;
    @InjectMocks
    CustomerService service;

    @Test
    void normalizeEmailTrimsAndLowercases() {
        assertThat(CustomerService.normalizeEmail("  Foo@BAR.com ")).isEqualTo("foo@bar.com");
    }

    @Test
    void createChecksUniquenessOnNormalizedEmail() {
        when(customerRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CustomerRequest("A", " A@B.com", null)))
                .isInstanceOf(ConflictException.class);
        verify(customerRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteIsRefusedWhenCustomerHasOrders() {
        Customer customer = new Customer("A", "a@b.com", null);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.existsByCustomerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(ConflictException.class);
        verify(customerRepository, never()).delete(any());
    }
}
