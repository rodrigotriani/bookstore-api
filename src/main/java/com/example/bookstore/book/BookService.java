package com.example.bookstore.book;

import com.example.bookstore.common.error.ConflictException;
import com.example.bookstore.common.error.ResourceNotFoundException;
import com.example.bookstore.order.OrderItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final OrderItemRepository orderItemRepository;

    public BookService(BookRepository bookRepository, OrderItemRepository orderItemRepository) {
        this.bookRepository = bookRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        String isbn = request.isbn().trim();
        if (bookRepository.existsByIsbn(isbn)) {
            throw duplicateIsbn(isbn);
        }
        Book book = new Book(request.title().trim(), request.author().trim(), isbn, request.price().setScale(2),
                request.stockQuantity());
        return BookResponse.from(bookRepository.saveAndFlush(book));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream().map(BookResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return BookResponse.from(getOrThrow(id));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getOrThrow(id);
        String isbn = request.isbn().trim();
        if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw duplicateIsbn(isbn);
        }
        book.setTitle(request.title().trim());
        book.setAuthor(request.author().trim());
        book.setIsbn(isbn);
        book.setPrice(request.price().setScale(2));
        book.setStockQuantity(request.stockQuantity());
        return BookResponse.from(bookRepository.saveAndFlush(book));
    }

    @Transactional
    public void delete(Long id) {
        Book book = getOrThrow(id);
        if (orderItemRepository.existsByBookId(id)) {
            throw new ConflictException("Book " + id + " is referenced by existing orders and cannot be deleted");
        }
        bookRepository.delete(book);
        bookRepository.flush();
    }

    private Book getOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " not found"));
    }

    private ConflictException duplicateIsbn(String isbn) {
        return new ConflictException("A book with ISBN " + isbn + " already exists");
    }
}
