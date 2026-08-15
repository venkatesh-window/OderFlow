package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Concrete implementation representing a Market Order (executes at best available price).
 */
public class MarketOrder extends Order {

    /**
     * Constructs a new MarketOrder.
     *
     * @param orderId the unique identifier for this order
     * @param trader  the trader submitting the order
     * @param side    whether this is a BUY or SELL order
     * @param quantity the amount to transact
     */
    public MarketOrder(String orderId, Trader trader, OrderSide side, long quantity) {
        this(orderId, trader, side, quantity, LocalDateTime.now());
    }

    /**
     * Constructs a new MarketOrder with a specific timestamp.
     *
     * @param orderId   the unique identifier for this order
     * @param trader    the trader submitting the order
     * @param side      whether this is a BUY or SELL order
     * @param quantity  the amount to transact
     * @param timestamp the time the order was created
     */
    public MarketOrder(String orderId, Trader trader, OrderSide side, long quantity, LocalDateTime timestamp) {
        super(orderId, trader, side, OrderType.MARKET, 0L, quantity, timestamp);
    }
}
