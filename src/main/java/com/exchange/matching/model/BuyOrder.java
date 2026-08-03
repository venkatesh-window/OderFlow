package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Concrete implementation representing a Buy Order (can be LIMIT or MARKET).
 */
public class BuyOrder extends Order {

    public BuyOrder(String orderId, Trader trader, long price, long quantity) {
        this(orderId, trader, OrderType.LIMIT, price, quantity, LocalDateTime.now());
    }

    public BuyOrder(String orderId, Trader trader, OrderType type, long price, long quantity) {
        this(orderId, trader, type, price, quantity, LocalDateTime.now());
    }

    public BuyOrder(String orderId, Trader trader, OrderType type, long price, long quantity, LocalDateTime timestamp) {
        super(orderId, trader, OrderSide.BUY, type, price, quantity, timestamp);
    }
}
