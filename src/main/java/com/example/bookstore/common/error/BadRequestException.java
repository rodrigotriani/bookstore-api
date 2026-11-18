package com.example.bookstore.common.error;

/** Maps to HTTP 400 for business-level request problems that bean validation cannot express. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
