package com.example.bookstore.order;

import com.example.bookstore.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A purchase order. Named BookOrder because ORDER is a reserved SQL word. */
@Entity
@Table(name = "book_orders")
public class BookOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    // Cascade is limited to persisting items: never cascades removal to books or customers.
    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    protected BookOrder() {
    }

    public BookOrder(Customer customer, Instant createdAt) {
        this.customer = customer;
        this.createdAt = createdAt;
        this.total = BigDecimal.ZERO;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        total = total.add(item.getSubtotal());
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
