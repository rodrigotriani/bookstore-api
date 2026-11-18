package com.example.bookstore.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Application liveness")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Returns UP when the application is running.")
    public HealthResponse health() {
        return new HealthResponse("UP");
    }

    public record HealthResponse(String status) {
    }
}
