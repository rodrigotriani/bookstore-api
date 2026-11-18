package com.example.bookstore.health;

import com.example.bookstore.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthControllerTest extends IntegrationTestSupport {

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"UP\"}", true));
    }

    @Test
    void openApiDocumentIsPublishedAndListsAllModules() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/health']").exists())
                .andExpect(jsonPath("$.paths['/api/books']").exists())
                .andExpect(jsonPath("$.paths['/api/customers']").exists())
                .andExpect(jsonPath("$.paths['/api/orders']").exists());
    }

    @Test
    void unknownRouteReturnsJsonNotFound() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
