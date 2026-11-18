package com.example.bookstore.customer;

import com.example.bookstore.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerApiTest extends IntegrationTestSupport {

    @Test
    void createReturns201AndNormalizesEmail() throws Exception {
        postJson("/api/customers", customerJson("Ada", "  Ada@Example.COM "))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ada"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.phone").value("555-0100"));
    }

    @Test
    void phoneIsOptional() throws Exception {
        postJson("/api/customers", "{\"name\":\"Bob\",\"email\":\"bob@example.com\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").doesNotExist());
    }

    @Test
    void listAndGet() throws Exception {
        long id = createCustomer("a@example.com");
        createCustomer("b@example.com");
        mockMvc.perform(get("/api/customers")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/customers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@example.com"));
    }

    @Test
    void getUnknownReturns404() throws Exception {
        mockMvc.perform(get("/api/customers/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer 9999 not found"));
    }

    @Test
    void invalidEmailAndBlankNameReturn400() throws Exception {
        postJson("/api/customers", customerJson("Ada", "not-an-email")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(org.hamcrest.Matchers.startsWith("email:")));
        postJson("/api/customers", customerJson(" ", "ok@example.com")).andExpect(status().isBadRequest());
        postJson("/api/customers", "{\"name\":\"X\"}").andExpect(status().isBadRequest());
    }

    @Test
    void duplicateEmailIsDetectedAfterNormalization() throws Exception {
        createCustomer("dup@example.com");
        postJson("/api/customers", customerJson("Other", " DUP@Example.com "))
                .andExpect(status().isConflict());
    }

    @Test
    void updateChangesFieldsAndAllowsOwnEmail() throws Exception {
        long id = createCustomer("u@example.com");
        putJson("/api/customers/" + id, customerJson("New Name", "U@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("u@example.com"));
    }

    @Test
    void updateErrors() throws Exception {
        createCustomer("first@example.com");
        long second = createCustomer("second@example.com");
        putJson("/api/customers/" + second, customerJson("N", "first@example.com")).andExpect(status().isConflict());
        putJson("/api/customers/" + second, customerJson("N", "bad")).andExpect(status().isBadRequest());
        putJson("/api/customers/9999", customerJson("N", "x@example.com")).andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204ThenNotFound() throws Exception {
        long id = createCustomer("del@example.com");
        mockMvc.perform(delete("/api/customers/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/customers/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/customers/" + id)).andExpect(status().isNotFound());
    }
}
