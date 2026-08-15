package com.exchange.matching.model;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderStatus;
import com.exchange.matching.enums.OrderType;

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

    /**
     * Constructs a new Order.
     *
     * @param orderId   the unique identifier for this order
     * @param trader    the trader submitting the order
     * @param side      whether this is a BUY or SELL order
     * @param type      whether this is a LIMIT or MARKET order
     * @param price     the price (0 for market orders)
     * @param quantity  the quantity to transact
     * @param timestamp the time the order was created
     */
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

    /** @return the unique identifier for this order */
    public String getOrderId() {
        return orderId;
    }

    /** @return the trader who submitted the order */
    public Trader getTrader() {
        return trader;
    }

    /** @return the side of the order (BUY or SELL) */
    public OrderSide getSide() {
        return side;
    }

    /** @param side the side of the order */
    public void setSide(OrderSide side) {
        this.side = side;
    }

    /** @return the type of the order (LIMIT or MARKET) */
    public OrderType getType() {
        return type;
    }

    /** @param type the type of the order */
    public void setType(OrderType type) {
        this.type = type;
    }

    /** @return the limit price of the order */
    public long getPrice() {
        return price;
    }

    /** @param price the limit price of the order */
    public void setPrice(long price) {
        this.price = price;
    }

    /** @return the original quantity of the order */
    public long getQuantity() {
        return quantity;
    }

    /** @param quantity the original quantity of the order */
    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    /** @return the remaining quantity to be filled */
    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    /** @param remainingQuantity the remaining quantity to be filled */
    public void setRemainingQuantity(long remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    /** @return the timestamp when the order was created */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /** @param timestamp the timestamp when the order was created */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /** @return the current status of the order */
    public OrderStatus getStatus() {
        return status;
    }

    /** @param status the current status of the order */
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /** @return true if the order is completely filled */
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
