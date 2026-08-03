package com.exchange.matching.enums;

/**
 * Represents the lifecycle status of an order in the matching engine.
 */
public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED
}
