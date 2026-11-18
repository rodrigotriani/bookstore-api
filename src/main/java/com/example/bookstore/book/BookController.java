package com.example.bookstore.book;

import com.example.bookstore.common.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book catalog and stock")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @Operation(summary = "Create a book")
    @ApiResponse(responseCode = "201", description = "Book created")
    @ApiResponse(responseCode = "400", description = "Invalid payload",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Duplicate ISBN",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        BookResponse created = bookService.create(request);
        return ResponseEntity.created(URI.create("/api/books/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List all books")
    @ApiResponse(responseCode = "200", description = "List of books")
    public List<BookResponse> list() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by id")
    @ApiResponse(responseCode = "200", description = "Book found")
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public BookResponse get(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book")
    @ApiResponse(responseCode = "200", description = "Book updated")
    @ApiResponse(responseCode = "400", description = "Invalid payload",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Duplicate ISBN",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Refused with 409 when the book appears in any order.")
    @ApiResponse(responseCode = "204", description = "Book deleted")
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Book referenced by orders",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
