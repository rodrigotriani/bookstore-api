package com.example.bookstore.common.error;

/** Maps to HTTP 409 (duplicates, referenced resources, insufficient stock). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
