package com.orderflow.model;

import com.orderflow.enums.OrderSide;
import com.orderflow.enums.OrderStatus;
import com.orderflow.enums.OrderType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class representing an order in the matching engine.
 */
public abstract class Order {
    private final String orderId;
    private final Trader trader;
    private OrderSide side;
    private OrderType type;
    private long price;
    private long quantity;
    private long remainingQuantity;
    private LocalDateTime timestamp;
    private OrderStatus status;

    public Order(String orderId, Trader trader, OrderSide side, OrderType type,
                 long price, long quantity, LocalDateTime timestamp) {
        this.orderId = orderId;
        this.trader = trader;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.status = OrderStatus.NEW;
    }

    public String getOrderId() {
        return orderId;
    }

    public Trader getTrader() {
        return trader;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(long remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public boolean isFilled() {
        return remainingQuantity == 0 || status == OrderStatus.FILLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, trader=%s, side=%s, type=%s, price=%d, qty=%d, remQty=%d, status=%s, time=%s]",
                getClass().getSimpleName(), orderId, (trader != null ? trader.getTraderId() : "null"),
                side, type, price, quantity, remainingQuantity, status, timestamp);
    }
}
