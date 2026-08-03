package com.exchange.matching.exception;

/**
 * Thrown when an error occurs during trade matching or execution in the matching engine.
 */
public class MatchingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatchingException(String message) {
        super(message);
    }

    public MatchingException(String message, Throwable cause) {
        super(message, cause);
    }
}
