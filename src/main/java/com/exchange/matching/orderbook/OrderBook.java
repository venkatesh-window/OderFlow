package com.exchange.matching.orderbook;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderStatus;
import com.exchange.matching.exception.OrderNotFoundException;
import com.exchange.matching.model.Order;
import com.exchange.matching.util.OrderComparator;
import com.exchange.matching.util.OrderPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Maintains the BUY and SELL order books using Price-Time Priority queues.
 * Provides O(1) order lookups via an internal map and logarithmic insertion/removal.
 */
public class OrderBook {
    private final PriorityQueue<Order> buyQueue;
    private final PriorityQueue<Order> sellQueue;
    private final Map<String, Order> orderMap;

    /**
     * Constructs a new OrderBook.
     */
    public OrderBook() {
        this.buyQueue = new PriorityQueue<>(OrderComparator.getBuyComparator());
        this.sellQueue = new PriorityQueue<>(OrderComparator.getSellComparator());
        this.orderMap = new HashMap<>();
    }

    /**
     * Stores an order in the appropriate BUY or SELL queue and tracks it by ID.
     *
     * @param order the order to store
     */
    public void addOrder(Order order) {
        orderMap.put(order.getOrderId(), order);
        if (order.getSide() == OrderSide.BUY) {
            buyQueue.add(order);
        } else {
            sellQueue.add(order);
        }
    }

    /**
     * Removes an order from the order book by its order ID.
     *
     * @param orderId the ID of the order to remove
     * @return the removed order
     * @throws OrderNotFoundException if the order does not exist in the book
     */
    public Order removeOrder(String orderId) {
        Order order = orderMap.remove(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found with ID: " + orderId);
        }
        if (order.getSide() == OrderSide.BUY) {
            buyQueue.remove(order);
        } else {
            sellQueue.remove(order);
        }
        return order;
    }

    /**
     * Cancels an existing order in the book by ID.
     *
     * @param orderId the ID of the order to cancel
     * @return the cancelled order
     * @throws OrderNotFoundException if the order does not exist in the book
     */
    public Order cancelOrder(String orderId) {
        Order order = removeOrder(orderId);
        order.setStatus(OrderStatus.CANCELLED);
        return order;
    }

    /**
     * Retrieves an order by ID without removing it.
     *
     * @param orderId the ID of the order
     * @return the order
     * @throws OrderNotFoundException if the order does not exist in the book
     */
    public Order getOrder(String orderId) {
        Order order = orderMap.get(orderId);
        if (order == null) {
            throw new OrderNotFoundException("Order not found with ID: " + orderId);
        }
        return order;
    }

    /**
     * Checks if an order ID currently exists in the order book.
     *
     * @param orderId the ID to check
     * @return true if present, false otherwise
     */
    public boolean containsOrder(String orderId) {
        return orderMap.containsKey(orderId);
    }

    /**
     * @return the best buy order, or null if none
     */
    public Order peekBestBuy() {
        return buyQueue.peek();
    }

    /**
     * @return the best sell order, or null if none
     */
    public Order peekBestSell() {
        return sellQueue.peek();
    }

    /**
     * @return removes and returns the best buy order
     */
    public Order pollBestBuy() {
        Order bestBuy = buyQueue.poll();
        if (bestBuy != null) {
            orderMap.remove(bestBuy.getOrderId());
        }
        return bestBuy;
    }

    /**
     * @return removes and returns the best sell order
     */
    public Order pollBestSell() {
        Order bestSell = sellQueue.poll();
        if (bestSell != null) {
            orderMap.remove(bestSell.getOrderId());
        }
        return bestSell;
    }

    /**
     * Returns a sorted list of current buy orders (without modifying the book).
     */
    /**
     * @return an unmodifiable list of all resting buy orders
     */
    public List<Order> getBuyOrders() {
        List<Order> sortedList = new ArrayList<>();
        PriorityQueue<Order> copy = new PriorityQueue<>(OrderComparator.getBuyComparator());
        copy.addAll(buyQueue);
        while (!copy.isEmpty()) {
            sortedList.add(copy.poll());
        }
        return sortedList;
    }

    /**
     * Returns a sorted list of current sell orders (without modifying the book).
     */
    /**
     * @return an unmodifiable list of all resting sell orders
     */
    public List<Order> getSellOrders() {
        List<Order> sortedList = new ArrayList<>();
        PriorityQueue<Order> copy = new PriorityQueue<>(OrderComparator.getSellComparator());
        copy.addAll(sellQueue);
        while (!copy.isEmpty()) {
            sortedList.add(copy.poll());
        }
        return sortedList;
    }

    /**
     * Prints the buy side of the order book.
     */
    public void printBuyOrders() {
        OrderPrinter.printBuyOrders(getBuyOrders());
    }

    /**
     * Prints the sell side of the order book.
     */
    public void printSellOrders() {
        OrderPrinter.printSellOrders(getSellOrders());
    }

    /**
     * Prints the full order book.
     */
    public void printOrderBook() {
        OrderPrinter.printOrderBook(getBuyOrders(), getSellOrders());
    }

    /**
     * @return the total number of orders in the book
     */
    public int size() {
        return orderMap.size();
    }

    /**
     * @return true if the book is empty
     */
    public boolean isEmpty() {
        return orderMap.isEmpty();
    }

    /**
     * Clears all orders from the book.
     */
    public void clear() {
        buyQueue.clear();
        sellQueue.clear();
        orderMap.clear();
    }
}
