package com.example.bookstore.order;

import com.example.bookstore.book.Book;
import com.example.bookstore.book.BookRepository;
import com.example.bookstore.common.error.BadRequestException;
import com.example.bookstore.common.error.ConflictException;
import com.example.bookstore.common.error.ResourceNotFoundException;
import com.example.bookstore.customer.Customer;
import com.example.bookstore.customer.CustomerRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    BookRepository bookRepository;
    @InjectMocks
    OrderService service;

    private static Book book(long id, String price, int stock) {
        Book book = new Book("T" + id, "A", "ISBN" + id, new BigDecimal(price), stock);
        org.springframework.test.util.ReflectionTestUtils.setField(book, "id", id);
        return book;
    }

    @Test
    void duplicateBookIdsAreRejectedBeforeAnyLookup() {
        OrderRequest request = new OrderRequest(1L,
                List.of(new OrderItemRequest(5L, 1), new OrderItemRequest(5L, 2)));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(customerRepository, bookRepository, orderRepository);
    }

    @Test
    void unknownCustomerIsNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new OrderRequest(1L, List.of(new OrderItemRequest(5L, 1)))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void unknownBookIsNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer("C", "c@e.com", null)));
        when(bookRepository.findAllByIdForUpdate(Set.of(5L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(new OrderRequest(1L, List.of(new OrderItemRequest(5L, 1)))))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void insufficientStockIsConflictAndLeavesStockUntouched() {
        Book b1 = book(1, "10.00", 5);
        Book b2 = book(2, "20.00", 1);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer("C", "c@e.com", null)));
        when(bookRepository.findAllByIdForUpdate(Set.of(1L, 2L))).thenReturn(List.of(b1, b2));

        OrderRequest request = new OrderRequest(1L,
                List.of(new OrderItemRequest(1L, 2), new OrderItemRequest(2L, 2)));
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ConflictException.class);

        assertThat(b1.getStockQuantity()).isEqualTo(5);
        assertThat(b2.getStockQuantity()).isEqualTo(1);
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void successfulOrderDecrementsStockAndComputesTotals() {
        Book b1 = book(1, "10.50", 5);
        Book b2 = book(2, "3.25", 4);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer("C", "c@e.com", null)));
        when(bookRepository.findAllByIdForUpdate(Set.of(1L, 2L))).thenReturn(List.of(b1, b2));
        when(orderRepository.saveAndFlush(any(BookOrder.class))).then(returnsFirstArg());

        OrderResponse response = service.create(new OrderRequest(1L,
                List.of(new OrderItemRequest(2L, 4), new OrderItemRequest(1L, 2))));

        assertThat(b1.getStockQuantity()).isEqualTo(3);
        assertThat(b2.getStockQuantity()).isZero();
        assertThat(response.items()).hasSize(2);
        assertThat(response.total()).isEqualByComparingTo("34.00");
    }
}
