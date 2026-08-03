package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Concrete implementation representing a Limit Order with a specified price.
 */
public class LimitOrder extends Order {

    public LimitOrder(String orderId, Trader trader, OrderSide side, long price, long quantity) {
        this(orderId, trader, side, price, quantity, LocalDateTime.now());
    }

    public LimitOrder(String orderId, Trader trader, OrderSide side, long price, long quantity, LocalDateTime timestamp) {
        super(orderId, trader, side, OrderType.LIMIT, price, quantity, timestamp);
    }
}
