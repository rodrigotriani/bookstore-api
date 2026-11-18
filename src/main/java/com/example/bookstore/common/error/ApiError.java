package com.example.bookstore.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Standard error response returned by every endpoint")
public record ApiError(
        @Schema(description = "Time the error occurred (UTC)") Instant timestamp,
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "HTTP reason phrase", example = "Not Found") String error,
        @Schema(description = "Human readable message", example = "Book 42 not found") String message,
        @Schema(description = "Request path", example = "/api/books/42") String path,
        @Schema(description = "Field-level validation details (empty when not applicable)") List<String> details) {
}
