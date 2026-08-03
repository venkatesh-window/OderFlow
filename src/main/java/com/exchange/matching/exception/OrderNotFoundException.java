package com.exchange.matching.exception;

/**
 * Thrown when attempting to cancel or modify an order that does not exist in the order book.
 */
public class OrderNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
