package com.example.bookstore.book;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "A book in the catalog")
public record BookResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Clean Code") String title,
        @Schema(example = "Robert C. Martin") String author,
        @Schema(example = "9780132350884") String isbn,
        @Schema(example = "39.90") BigDecimal price,
        @Schema(example = "10") int stockQuantity) {

    static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getPrice(),
                book.getStockQuantity());
    }
}
