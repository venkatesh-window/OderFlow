package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Represents a limit order in the matching engine.
 */
public class LimitOrder extends Order {

    /**
     * Constructs a new LimitOrder.
     *
     * @param orderId  the unique identifier for this order
     * @param trader   the trader submitting the order
     * @param side     whether this is a BUY or SELL order
     * @param price    the limit price
     * @param quantity the amount to transact
     */
    public LimitOrder(String orderId, Trader trader, OrderSide side, long price, long quantity) {
        this(orderId, trader, side, price, quantity, LocalDateTime.now());
    }

    /**
     * Constructs a new LimitOrder with full parameters.
     *
     * @param orderId   the unique identifier for this order
     * @param trader    the trader submitting the order
     * @param side      whether this is a BUY or SELL order
     * @param price     the limit price
     * @param quantity  the amount to transact
     * @param timestamp the time the order was created
     */
    public LimitOrder(String orderId, Trader trader, OrderSide side, long price, long quantity, LocalDateTime timestamp) {
        super(orderId, trader, side, OrderType.LIMIT, price, quantity, timestamp);
    }
}
