package com.example.bookstore.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<BookOrder, Long> {

    boolean existsByCustomerId(Long customerId);

    @Override
    @EntityGraph(attributePaths = {"customer", "items", "items.book"})
    List<BookOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "items", "items.book"})
    Optional<BookOrder> findById(Long id);
}
