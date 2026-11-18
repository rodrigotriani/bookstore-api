package com.example.bookstore.book;

import com.example.bookstore.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookApiTest extends IntegrationTestSupport {

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        postJson("/api/books", bookJson("Clean Code", "9780132350884", "39.90", 10))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/books/" + bookRepository.findAll().get(0).getId())))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"))
                .andExpect(jsonPath("$.price").value(39.90))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void listAndGetReturnCreatedBooks() throws Exception {
        long id = createBook("111", "10.00", 1);
        createBook("222", "20.00", 2);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].isbn", hasItem("111")));
        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("111"));
    }

    @Test
    void getUnknownBookReturns404WithErrorModel() throws Exception {
        mockMvc.perform(get("/api/books/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book 9999 not found"))
                .andExpect(jsonPath("$.path").value("/api/books/9999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidPayloadReturns400WithFieldDetails() throws Exception {
        postJson("/api/books", """
                {"title":" ","author":"","isbn":"","price":0,"stockQuantity":-1}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details", hasSize(5)))
                .andExpect(jsonPath("$.details[*]", hasItem(org.hamcrest.Matchers.startsWith("price:"))))
                .andExpect(jsonPath("$.details[*]", hasItem(org.hamcrest.Matchers.startsWith("stockQuantity:"))));
    }

    @Test
    void missingPriceAndMalformedJsonReturn400() throws Exception {
        postJson("/api/books", "{\"title\":\"T\",\"author\":\"A\",\"isbn\":\"1\",\"stockQuantity\":1}")
                .andExpect(status().isBadRequest());
        postJson("/api/books", "{not json").andExpect(status().isBadRequest());
    }

    @Test
    void priceWithTooManyDecimalsReturns400() throws Exception {
        postJson("/api/books", bookJson("T", "1", "10.999", 1)).andExpect(status().isBadRequest());
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/books/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void duplicateIsbnOnCreateReturns409() throws Exception {
        createBook("DUP-1", "10.00", 1);
        postJson("/api/books", bookJson("Other", "DUP-1", "5.00", 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void updateChangesFieldsAndKeepsOwnIsbn() throws Exception {
        long id = createBook("UPD-1", "10.00", 1);
        putJson("/api/books/" + id, bookJson("New Title", "UPD-1", "12.50", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.stockQuantity").value(7));
    }

    @Test
    void updateWithAnotherBooksIsbnReturns409() throws Exception {
        createBook("A-1", "10.00", 1);
        long second = createBook("B-1", "10.00", 1);
        putJson("/api/books/" + second, bookJson("T", "A-1", "10.00", 1)).andExpect(status().isConflict());
    }

    @Test
    void updateUnknownReturns404AndInvalidReturns400() throws Exception {
        putJson("/api/books/9999", bookJson("T", "X", "1.00", 1)).andExpect(status().isNotFound());
        long id = createBook("V-1", "10.00", 1);
        putJson("/api/books/" + id, bookJson("", "V-1", "1.00", 1)).andExpect(status().isBadRequest());
    }

    @Test
    void deleteReturns204ThenGetReturns404() throws Exception {
        long id = createBook("DEL-1", "10.00", 1);
        mockMvc.perform(delete("/api/books/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/books/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownReturns404() throws Exception {
        mockMvc.perform(delete("/api/books/9999")).andExpect(status().isNotFound());
    }
}
