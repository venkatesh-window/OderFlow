package com.orderflow.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an executed trade resulting from a match between a buy order and a sell order.
 */
public class Trade {
    private final String tradeId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final long price;
    private final long quantity;
    private final LocalDateTime timestamp;

    public Trade(String buyOrderId, String sellOrderId, long price, long quantity) {
        this(UUID.randomUUID().toString(), buyOrderId, sellOrderId, price, quantity, LocalDateTime.now());
    }

    public Trade(String tradeId, String buyOrderId, String sellOrderId, long price, long quantity, LocalDateTime timestamp) {
        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trade)) return false;
        Trade trade = (Trade) o;
        return Objects.equals(tradeId, trade.tradeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId);
    }

    @Override
    public String toString() {
        return String.format("Trade[id=%s, buyOrderId=%s, sellOrderId=%s, price=%d, qty=%d, time=%s]",
                tradeId, buyOrderId, sellOrderId, price, quantity, timestamp);
    }
}
