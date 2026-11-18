package com.example.bookstore.order;

import com.example.bookstore.book.Book;
import com.example.bookstore.book.BookRepository;
import com.example.bookstore.common.error.BadRequestException;
import com.example.bookstore.common.error.ConflictException;
import com.example.bookstore.common.error.ResourceNotFoundException;
import com.example.bookstore.customer.Customer;
import com.example.bookstore.customer.CustomerRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository,
            BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Places an order atomically. Book rows are locked with {@code SELECT ... FOR UPDATE} in ascending id order
     * (consistent lock ordering avoids deadlocks) inside this single transaction; stock is validated and
     * decremented under the lock, so concurrent orders can never oversell. Any exception rolls everything back.
     */
    @Transactional
    public OrderResponse create(OrderRequest request) {
        Set<Long> seen = new HashSet<>();
        for (OrderItemRequest item : request.items()) {
            if (!seen.add(item.bookId())) {
                throw new BadRequestException("Duplicate book id " + item.bookId()
                        + " in order items; send one line per book with the total quantity");
            }
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + request.customerId() + " not found"));

        Set<Long> sortedBookIds = new TreeSet<>(seen);
        Map<Long, Book> books = bookRepository.findAllByIdForUpdate(sortedBookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        for (Long bookId : sortedBookIds) {
            if (!books.containsKey(bookId)) {
                throw new ResourceNotFoundException("Book " + bookId + " not found");
            }
        }
        for (OrderItemRequest item : request.items()) {
            Book book = books.get(item.bookId());
            if (book.getStockQuantity() < item.quantity()) {
                throw new ConflictException("Insufficient stock for book " + book.getId() + ": requested "
                        + item.quantity() + ", available " + book.getStockQuantity());
            }
        }

        BookOrder order = new BookOrder(customer, Instant.now());
        for (OrderItemRequest item : request.items()) {
            Book book = books.get(item.bookId());
            book.setStockQuantity(book.getStockQuantity() - item.quantity());
            order.addItem(new OrderItem(order, book, item.quantity(), book.getPrice()));
        }
        BookOrder saved = orderRepository.saveAndFlush(order);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderRepository.findById(id).map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found"));
    }
}
