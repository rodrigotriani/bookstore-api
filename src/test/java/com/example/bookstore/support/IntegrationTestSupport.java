package com.example.bookstore.support;

import com.example.bookstore.book.BookRepository;
import com.example.bookstore.customer.CustomerRepository;
import com.example.bookstore.order.OrderItemRepository;
import com.example.bookstore.order.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Shared Spring Boot + MockMvc setup. Each test starts from an empty database. */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected OrderItemRepository orderItemRepository;
    @Autowired
    protected OrderRepository orderRepository;
    @Autowired
    protected BookRepository bookRepository;
    @Autowired
    protected CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
    }

    protected ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions putJson(String url, String json) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected static String bookJson(String title, String isbn, String price, int stock) {
        return """
                {"title":"%s","author":"Some Author","isbn":"%s","price":%s,"stockQuantity":%d}"""
                .formatted(title, isbn, price, stock);
    }

    protected static String customerJson(String name, String email) {
        return """
                {"name":"%s","email":"%s","phone":"555-0100"}""".formatted(name, email);
    }

    protected long createBook(String isbn, String price, int stock) throws Exception {
        String body = postJson("/api/books", bookJson("Title " + isbn, isbn, price, stock))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    protected long createCustomer(String email) throws Exception {
        String body = postJson("/api/customers", customerJson("Customer", email))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    protected static String orderJson(long customerId, long... bookIdAndQuantityPairs) {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < bookIdAndQuantityPairs.length; i += 2) {
            if (i > 0) {
                items.append(',');
            }
            items.append("{\"bookId\":").append(bookIdAndQuantityPairs[i])
                    .append(",\"quantity\":").append(bookIdAndQuantityPairs[i + 1]).append('}');
        }
        return "{\"customerId\":" + customerId + ",\"items\":[" + items + "]}";
    }
}
