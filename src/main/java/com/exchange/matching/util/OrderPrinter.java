package com.exchange.matching.util;

import com.exchange.matching.model.Order;
import com.exchange.matching.model.Trade;

import java.util.List;

/**
 * Utility class for formatted console printing of buy orders, sell orders, the full order book, and trade history.
 */
public class OrderPrinter {

    private OrderPrinter() {
        // Utility class
    }

    public static void printBuyOrders(List<Order> buyOrders) {
        System.out.println("=== BUY ORDERS (Price-Time Priority) ===");
        if (buyOrders.isEmpty()) {
            System.out.println("  [Empty]");
        } else {
            for (Order order : buyOrders) {
                System.out.println("  " + order);
            }
        }
        System.out.println("========================================");
    }

    public static void printSellOrders(List<Order> sellOrders) {
        System.out.println("=== SELL ORDERS (Price-Time Priority) ===");
        if (sellOrders.isEmpty()) {
            System.out.println("  [Empty]");
        } else {
            for (Order order : sellOrders) {
                System.out.println("  " + order);
            }
        }
        System.out.println("=========================================");
    }

    public static void printOrderBook(List<Order> buyOrders, List<Order> sellOrders) {
        System.out.println("\n########################################");
        System.out.println("########## CURRENT ORDER BOOK ##########");
        System.out.println("########################################");
        printBuyOrders(buyOrders);
        printSellOrders(sellOrders);
        System.out.println("########################################\n");
    }

    public static void printTradeHistory(List<Trade> trades) {
        System.out.println("=== EXECUTED TRADE HISTORY ===");
        if (trades.isEmpty()) {
            System.out.println("  [No Trades Executed]");
        } else {
            for (Trade trade : trades) {
                System.out.println("  " + trade);
            }
        }
        System.out.println("==============================");
    }
}
