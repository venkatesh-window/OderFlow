package com.orderflow.orderbook;

import com.orderflow.enums.OrderSide;
import com.orderflow.enums.OrderStatus;
import com.orderflow.exception.OrderNotFoundException;
import com.orderflow.model.Order;
import com.orderflow.util.OrderComparator;
import com.orderflow.util.OrderPrinter;

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

    public Order peekBestBuy() {
        return buyQueue.peek();
    }

    public Order peekBestSell() {
        return sellQueue.peek();
    }

    public Order pollBestBuy() {
        Order bestBuy = buyQueue.poll();
        if (bestBuy != null) {
            orderMap.remove(bestBuy.getOrderId());
        }
        return bestBuy;
    }

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
    public List<Order> getSellOrders() {
        List<Order> sortedList = new ArrayList<>();
        PriorityQueue<Order> copy = new PriorityQueue<>(OrderComparator.getSellComparator());
        copy.addAll(sellQueue);
        while (!copy.isEmpty()) {
            sortedList.add(copy.poll());
        }
        return sortedList;
    }

    public void printBuyOrders() {
        OrderPrinter.printBuyOrders(getBuyOrders());
    }

    public void printSellOrders() {
        OrderPrinter.printSellOrders(getSellOrders());
    }

    public void printOrderBook() {
        OrderPrinter.printOrderBook(getBuyOrders(), getSellOrders());
    }

    public int size() {
        return orderMap.size();
    }

    public boolean isEmpty() {
        return orderMap.isEmpty();
    }

    public void clear() {
        buyQueue.clear();
        sellQueue.clear();
        orderMap.clear();
    }
}
