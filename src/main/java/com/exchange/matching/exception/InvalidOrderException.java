package com.exchange.matching.exception;

/**
 * Thrown when an order fails validation (e.g., negative price, zero quantity, null trader).
 */
public class InvalidOrderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderException(String message) {
        super(message);
    }

    public InvalidOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
