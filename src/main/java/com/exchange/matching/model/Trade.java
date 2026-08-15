package com.exchange.matching.model;
// Triggering IDE refresh for Trade

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an executed trade resulting from a match between a buy order and a sell order.
 */
public class Trade {
    private String tradeId;
    private String buyOrderId;
    private String sellOrderId;
    private long price;
    private long quantity;
    private LocalDateTime timestamp;

    public Trade() {
        // Default constructor for ObjectPool initialization
    }

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

    /**
     * Resets the trade object for reuse in a Zero-GC Object Pool.
     */
    public void reset(String tradeId, String buyOrderId, String sellOrderId, long price, long quantity) {
        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now(); // We can optimize this further if needed, but this allows reuse
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
