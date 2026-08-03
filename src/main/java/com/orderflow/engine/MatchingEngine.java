package com.orderflow.engine;

import com.orderflow.enums.OrderSide;
import com.orderflow.enums.OrderStatus;
import com.orderflow.enums.OrderType;
import com.orderflow.exception.MatchingException;
import com.orderflow.history.TradeHistory;
import com.orderflow.model.Order;
import com.orderflow.model.Trade;
import com.orderflow.orderbook.OrderBook;

import java.util.ArrayList;
import java.util.List;

/**
 * Core matching engine responsible for executing trades between Buy and Sell orders
 * according to Price-Time Priority and Exchange matching rules.
 */
public class MatchingEngine {

    /**
     * Attempts to match an incoming order against resting orders in the opposite order book.
     *
     * @param incomingOrder the newly submitted or modified order
     * @param orderBook     the current order book containing resting orders
     * @param tradeHistory  the trade history manager to record executed trades
     * @return list of trades generated during this matching execution
     * @throws MatchingException if an unexpected error occurs during matching
     */
    public List<Trade> match(Order incomingOrder, OrderBook orderBook, TradeHistory tradeHistory) {
        if (incomingOrder == null || orderBook == null || tradeHistory == null) {
            throw new MatchingException("Matching parameters (order, book, history) cannot be null.");
        }

        List<Trade> executedTrades = new ArrayList<>();

        try {
            while (incomingOrder.getRemainingQuantity() > 0) {
                Order oppositeOrder;
                if (incomingOrder.getSide() == OrderSide.BUY) {
                    oppositeOrder = orderBook.peekBestSell();
                } else {
                    oppositeOrder = orderBook.peekBestBuy();
                }

                // If no opposite orders exist in the book, matching stops
                if (oppositeOrder == null) {
                    break;
                }

                // Check if prices cross (matching rules)
                if (!canMatch(incomingOrder, oppositeOrder)) {
                    break;
                }

                // Determine trade execution price (Existing Order Price in the book has priority)
                long tradePrice = determineTradePrice(incomingOrder, oppositeOrder);

                // Trade quantity is minimum of both remaining quantities
                long tradeQuantity = Math.min(incomingOrder.getRemainingQuantity(), oppositeOrder.getRemainingQuantity());

                // Update remaining quantities
                incomingOrder.setRemainingQuantity(incomingOrder.getRemainingQuantity() - tradeQuantity);
                oppositeOrder.setRemainingQuantity(oppositeOrder.getRemainingQuantity() - tradeQuantity);

                // Update status of resting opposite order
                if (oppositeOrder.getRemainingQuantity() == 0) {
                    oppositeOrder.setStatus(OrderStatus.FILLED);
                    if (incomingOrder.getSide() == OrderSide.BUY) {
                        orderBook.pollBestSell();
                    } else {
                        orderBook.pollBestBuy();
                    }
                } else {
                    oppositeOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
                }

                // Update status of incoming order
                if (incomingOrder.getRemainingQuantity() == 0) {
                    incomingOrder.setStatus(OrderStatus.FILLED);
                } else {
                    incomingOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
                }

                // Create Trade record
                String buyOrderId = (incomingOrder.getSide() == OrderSide.BUY)
                        ? incomingOrder.getOrderId() : oppositeOrder.getOrderId();
                String sellOrderId = (incomingOrder.getSide() == OrderSide.SELL)
                        ? incomingOrder.getOrderId() : oppositeOrder.getOrderId();

                Trade trade = new Trade(buyOrderId, sellOrderId, tradePrice, tradeQuantity);
                tradeHistory.addTrade(trade);
                executedTrades.add(trade);
            }
        } catch (Exception e) {
            throw new MatchingException("Error occurred during trade matching: " + e.getMessage(), e);
        }

        return executedTrades;
    }

    /**
     * Determines whether two opposite orders can match based on price rules:
     * - Limit Buy matches Lowest Sell only if Buy Price >= Sell Price
     * - Limit Sell matches Highest Buy only if Sell Price <= Buy Price
     * - Market orders match any available best price
     */
    private boolean canMatch(Order incomingOrder, Order oppositeOrder) {
        if (incomingOrder.getType() == OrderType.MARKET || oppositeOrder.getType() == OrderType.MARKET) {
            return true;
        }
        if (incomingOrder.getSide() == OrderSide.BUY) {
            return incomingOrder.getPrice() >= oppositeOrder.getPrice();
        } else {
            return oppositeOrder.getPrice() >= incomingOrder.getPrice();
        }
    }

    /**
     * Trade Price is determined by the existing resting order price in the book.
     */
    private long determineTradePrice(Order incomingOrder, Order restingOrder) {
        if (restingOrder.getPrice() > 0) {
            return restingOrder.getPrice();
        }
        if (incomingOrder.getPrice() > 0) {
            return incomingOrder.getPrice();
        }
        return 0L;
    }
}
