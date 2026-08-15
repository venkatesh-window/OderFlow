package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Represents a sell order in the matching engine.
 */
public class SellOrder extends Order {

    /**
     * Constructs a new SellOrder (Limit).
     *
     * @param orderId  the unique identifier for this order
     * @param trader   the trader submitting the order
     * @param price    the limit price
     * @param quantity the amount to transact
     */
    public SellOrder(String orderId, Trader trader, long price, long quantity) {
        this(orderId, trader, OrderType.LIMIT, price, quantity, LocalDateTime.now());
    }

    /**
     * Constructs a new SellOrder with a specific type.
     *
     * @param orderId  the unique identifier for this order
     * @param trader   the trader submitting the order
     * @param type     LIMIT or MARKET
     * @param price    the limit price (0 for market)
     * @param quantity the amount to transact
     */
    public SellOrder(String orderId, Trader trader, OrderType type, long price, long quantity) {
        this(orderId, trader, type, price, quantity, LocalDateTime.now());
    }

    /**
     * Constructs a new SellOrder with full parameters.
     *
     * @param orderId   the unique identifier for this order
     * @param trader    the trader submitting the order
     * @param type      LIMIT or MARKET
     * @param price     the limit price (0 for market)
     * @param quantity  the amount to transact
     * @param timestamp the time the order was created
     */
    public SellOrder(String orderId, Trader trader, OrderType type, long price, long quantity, LocalDateTime timestamp) {
        super(orderId, trader, OrderSide.SELL, type, price, quantity, timestamp);
    }
}
