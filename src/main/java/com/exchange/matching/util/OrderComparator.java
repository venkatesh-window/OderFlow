package com.exchange.matching.util;

import com.exchange.matching.enums.OrderType;
import com.exchange.matching.model.Order;

import java.util.Comparator;

/**
 * Provides Comparators for Buy and Sell order books following strict Price-Time Priority.
 */
public class OrderComparator {

    private OrderComparator() {
        // Utility class
    }

    /**
     * BUY Queue Comparator:
     * 1. Highest price first (MARKET orders treated as Long.MAX_VALUE)
     * 2. If same price, earlier timestamp first (FIFO)
     *
     * @return a comparator for sorting buy orders
     */
    public static Comparator<Order> getBuyComparator() {
        return (o1, o2) -> {
            long price1 = o1.getType() == OrderType.MARKET ? Long.MAX_VALUE : o1.getPrice();
            long price2 = o2.getType() == OrderType.MARKET ? Long.MAX_VALUE : o2.getPrice();

            int priceCompare = Long.compare(price2, price1); // Descending order
            if (priceCompare != 0) {
                return priceCompare;
            }
            return o1.getTimestamp().compareTo(o2.getTimestamp()); // Ascending timestamp
        };
    }

    /**
     * SELL Queue Comparator:
     * 1. Lowest price first (MARKET orders treated as Long.MIN_VALUE)
     * 2. If same price, earlier timestamp first (FIFO)
     *
     * @return a comparator for sorting sell orders
     */
    public static Comparator<Order> getSellComparator() {
        return (o1, o2) -> {
            long price1 = o1.getType() == OrderType.MARKET ? Long.MIN_VALUE : o1.getPrice();
            long price2 = o2.getType() == OrderType.MARKET ? Long.MIN_VALUE : o2.getPrice();

            int priceCompare = Long.compare(price1, price2); // Ascending order
            if (priceCompare != 0) {
                return priceCompare;
            }
            return o1.getTimestamp().compareTo(o2.getTimestamp()); // Ascending timestamp
        };
    }
}
