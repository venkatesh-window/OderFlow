package com.exchange.matching.engine;

import com.exchange.matching.enums.OrderStatus;
import com.exchange.matching.enums.OrderType;
import com.exchange.matching.exception.InvalidOrderException;
import com.exchange.matching.exception.OrderNotFoundException;
import com.exchange.matching.history.TradeHistory;
import com.exchange.matching.model.Order;
import com.exchange.matching.model.Trade;
import com.exchange.matching.orderbook.OrderBook;
import com.exchange.matching.validator.OrderValidator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Central Exchange facade coordinating order validation, the order book,
 * the matching engine, and trade history.
 */
public class Exchange {
    private final OrderBook orderBook;
    private final MatchingEngine matchingEngine;
    private final OrderValidator validator;
    private final TradeHistory tradeHistory;

    public Exchange() {
        this.orderBook = new OrderBook();
        this.matchingEngine = new MatchingEngine();
        this.validator = new OrderValidator();
        this.tradeHistory = new TradeHistory();
    }

    public Exchange(OrderBook orderBook, MatchingEngine matchingEngine, OrderValidator validator, TradeHistory tradeHistory) {
        this.orderBook = orderBook;
        this.matchingEngine = matchingEngine;
        this.validator = validator;
        this.tradeHistory = tradeHistory;
    }

    /**
     * Submits a new order to the exchange: validates, matches against existing book,
     * and stores any remaining quantity in the order book.
     *
     * @param order the order to submit
     * @return list of trades executed immediately upon submission
     */
    public List<Trade> addOrder(Order order) {
        validator.validate(order, orderBook);
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.NEW);
        }

        List<Trade> trades = new java.util.ArrayList<>();
        matchingEngine.match(order, orderBook, tradeHistory, trades);

        if (order.getRemainingQuantity() > 0) {
            orderBook.addOrder(order);
        }

        return trades;
    }

    /**
     * Cancels a resting order in the order book by its ID.
     *
     * @param orderId the ID of the order to cancel
     * @return the cancelled order
     * @throws OrderNotFoundException if the order does not exist in the order book
     */
    public Order cancelOrder(String orderId) {
        return orderBook.cancelOrder(orderId);
    }

    /**
     * Modifies a resting order's price and quantity. Modifying an order updates its timestamp
     * to the current time (losing time priority) and attempts to match it immediately.
     *
     * @param orderId     the ID of the order to modify
     * @param newPrice    the new price
     * @param newQuantity the new total quantity
     * @return list of trades executed upon modification
     * @throws OrderNotFoundException if the order does not exist in the order book
     * @throws InvalidOrderException  if new price or quantity are invalid
     */
    public List<Trade> modifyOrder(String orderId, long newPrice, long newQuantity) {
        if (newQuantity <= 0) {
            throw new InvalidOrderException("Modified quantity must be greater than zero.");
        }

        // Remove from book first to re-insert or match with updated priority
        Order order = orderBook.removeOrder(orderId);

        if (order.getType() == OrderType.LIMIT && newPrice <= 0) {
            // Restore order to book if validation fails
            orderBook.addOrder(order);
            throw new InvalidOrderException("Modified limit order price must be greater than zero.");
        }
        if (newPrice < 0) {
            orderBook.addOrder(order);
            throw new InvalidOrderException("Modified price cannot be negative.");
        }

        order.setPrice(newPrice);
        order.setQuantity(newQuantity);
        order.setRemainingQuantity(newQuantity);
        order.setTimestamp(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);

        List<Trade> trades = new java.util.ArrayList<>();
        matchingEngine.match(order, orderBook, tradeHistory, trades);

        if (order.getRemainingQuantity() > 0) {
            orderBook.addOrder(order);
        }

        return trades;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public TradeHistory getTradeHistory() {
        return tradeHistory;
    }

    public MatchingEngine getMatchingEngine() {
        return matchingEngine;
    }

    public OrderValidator getValidator() {
        return validator;
    }

    public void printBuyOrders() {
        orderBook.printBuyOrders();
    }

    public void printSellOrders() {
        orderBook.printSellOrders();
    }

    public void printOrderBook() {
        orderBook.printOrderBook();
    }

    public void printTradeHistory() {
        tradeHistory.printTradeHistory();
    }
}
