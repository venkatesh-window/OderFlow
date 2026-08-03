package com.orderflow.validator;

import com.orderflow.enums.OrderType;
import com.orderflow.exception.DuplicateOrderException;
import com.orderflow.exception.InvalidOrderException;
import com.orderflow.model.Order;
import com.orderflow.orderbook.OrderBook;

/**
 * Validates incoming orders against exchange rules before submission to the order book.
 */
public class OrderValidator {

    /**
     * Validates an order for correctness and checks for duplicate IDs in the order book.
     *
     * @param order     the order to validate
     * @param orderBook the order book to check for duplicate order ID
     * @throws InvalidOrderException   if the order violates basic validation rules
     * @throws DuplicateOrderException if the order ID already exists in the order book
     */
    public void validate(Order order, OrderBook orderBook) {
        if (order == null) {
            throw new InvalidOrderException("Order cannot be null.");
        }
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            throw new InvalidOrderException("Order ID cannot be null or empty.");
        }
        if (order.getTrader() == null) {
            throw new InvalidOrderException("Trader cannot be null.");
        }
        if (order.getSide() == null) {
            throw new InvalidOrderException("OrderSide cannot be null.");
        }
        if (order.getType() == null) {
            throw new InvalidOrderException("OrderType cannot be null.");
        }
        if (order.getQuantity() <= 0 || order.getRemainingQuantity() <= 0) {
            throw new InvalidOrderException("Order quantity must be greater than zero.");
        }
        if (order.getPrice() < 0) {
            throw new InvalidOrderException("Price cannot be negative.");
        }
        if (order.getType() == OrderType.LIMIT && order.getPrice() <= 0) {
            throw new InvalidOrderException("Limit order price must be greater than zero.");
        }
        if (orderBook != null && orderBook.containsOrder(order.getOrderId())) {
            throw new DuplicateOrderException("Duplicate order ID: " + order.getOrderId());
        }
    }
}
