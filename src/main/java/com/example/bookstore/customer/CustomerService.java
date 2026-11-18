package com.example.bookstore.customer;

import com.example.bookstore.common.error.ConflictException;
import com.example.bookstore.common.error.ResourceNotFoundException;
import com.example.bookstore.order.OrderRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmail(email)) {
            throw duplicateEmail(email);
        }
        Customer customer = new Customer(request.name().trim(), email, normalizePhone(request.phone()));
        return CustomerResponse.from(customerRepository.saveAndFlush(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(CustomerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return CustomerResponse.from(getOrThrow(id));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getOrThrow(id);
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailAndIdNot(email, id)) {
            throw duplicateEmail(email);
        }
        customer.setName(request.name().trim());
        customer.setEmail(email);
        customer.setPhone(normalizePhone(request.phone()));
        return CustomerResponse.from(customerRepository.saveAndFlush(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = getOrThrow(id);
        if (orderRepository.existsByCustomerId(id)) {
            throw new ConflictException("Customer " + id + " has orders and cannot be deleted");
        }
        customerRepository.delete(customer);
        customerRepository.flush();
    }

    /** Email policy: trim surrounding whitespace and lowercase (Locale.ROOT) before lookup and persistence. */
    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    private Customer getOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + id + " not found"));
    }

    private ConflictException duplicateEmail(String email) {
        return new ConflictException("A customer with email " + email + " already exists");
    }
}
