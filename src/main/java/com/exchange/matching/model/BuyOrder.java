package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Represents a buy order in the matching engine.
 */
public class BuyOrder extends Order {

    /**
     * Constructs a new BuyOrder (Limit).
     *
     * @param orderId  the unique identifier for this order
     * @param trader   the trader submitting the order
     * @param price    the limit price
     * @param quantity the amount to transact
     */
    public BuyOrder(String orderId, Trader trader, long price, long quantity) {
        this(orderId, trader, OrderType.LIMIT, price, quantity, LocalDateTime.now());
    }

    /**
     * Constructs a new BuyOrder with a specific type.
     *
     * @param orderId  the unique identifier for this order
     * @param trader   the trader submitting the order
     * @param type     LIMIT or MARKET
     * @param price    the limit price (0 for market)
     * @param quantity the amount to transact
     */
    public BuyOrder(String orderId, Trader trader, OrderType type, long price, long quantity) {
        this(orderId, trader, type, price, quantity, LocalDateTime.now());
    }

    /**
     * Constructs a new BuyOrder with full parameters.
     *
     * @param orderId   the unique identifier for this order
     * @param trader    the trader submitting the order
     * @param type      LIMIT or MARKET
     * @param price     the limit price (0 for market)
     * @param quantity  the amount to transact
     * @param timestamp the time the order was created
     */
    public BuyOrder(String orderId, Trader trader, OrderType type, long price, long quantity, LocalDateTime timestamp) {
        super(orderId, trader, OrderSide.BUY, type, price, quantity, timestamp);
    }
}
