package com.exchange.matching.exception;

/**
 * Thrown when an order with an already existing order ID is submitted to the matching engine.
 */
public class DuplicateOrderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateOrderException(String message) {
        super(message);
    }

    public DuplicateOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
