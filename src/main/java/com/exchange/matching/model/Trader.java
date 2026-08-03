package com.exchange.matching.model;

import java.util.Objects;

/**
 * Represents a market participant (trader/client) submitting orders to the matching engine.
 */
public class Trader {
    private final String traderId;
    private final String name;

    public Trader(String traderId, String name) {
        if (traderId == null || traderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Trader ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Trader name cannot be null or empty.");
        }
        this.traderId = traderId;
        this.name = name;
    }

    public String getTraderId() {
        return traderId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trader)) return false;
        Trader trader = (Trader) o;
        return Objects.equals(traderId, trader.traderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traderId);
    }

    @Override
    public String toString() {
        return "Trader{" +
                "traderId='" + traderId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
