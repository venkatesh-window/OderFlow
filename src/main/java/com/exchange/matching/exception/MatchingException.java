package com.exchange.matching.exception;

/**
 * Thrown when an error occurs during trade matching or execution in the matching engine.
 */
public class MatchingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new MatchingException with the specified message.
     *
     * @param message the detail message
     */
    public MatchingException(String message) {
        super(message);
    }

    /**
     * Constructs a new MatchingException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public MatchingException(String message, Throwable cause) {
        super(message, cause);
    }
}
