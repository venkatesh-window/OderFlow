package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderType;

import java.time.LocalDateTime;

/**
 * Concrete implementation representing a Market Order (executes at best available price).
 */
public class MarketOrder extends Order {

    public MarketOrder(String orderId, Trader trader, OrderSide side, long quantity) {
        this(orderId, trader, side, quantity, LocalDateTime.now());
    }

    public MarketOrder(String orderId, Trader trader, OrderSide side, long quantity, LocalDateTime timestamp) {
        super(orderId, trader, side, OrderType.MARKET, 0L, quantity, timestamp);
    }
}
