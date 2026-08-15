package com.exchange.matching.engine;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.enums.OrderStatus;
import com.exchange.matching.enums.OrderType;
import com.exchange.matching.exception.MatchingException;
import com.exchange.matching.history.TradeHistory;
import com.exchange.matching.model.Order;
import com.exchange.matching.model.Trade;
import com.exchange.matching.orderbook.OrderBook;

import com.exchange.matching.util.ObjectPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Core matching engine responsible for executing trades between Buy and Sell orders
 * according to Price-Time Priority and Exchange matching rules.
 */
public class MatchingEngine {

    private final ObjectPool<Trade> tradePool;

    /**
     * Constructs a new MatchingEngine, initializing the object pool for trades.
     */
    public MatchingEngine() {
        this.tradePool = new ObjectPool<>(Trade::new, 1024);
    }

    /**
     * Attempts to match an incoming order against resting orders in the opposite order book.
     *
     * @param incomingOrder the newly submitted or modified order
     * @param orderBook     the current order book containing resting orders
     * @param tradeHistory  the trade history manager to record executed trades
     * @param executedTrades the list to populate with trades generated during this matching execution
     * @throws MatchingException if an unexpected error occurs during matching
     */
    public void match(Order incomingOrder, OrderBook orderBook, TradeHistory tradeHistory, List<Trade> executedTrades) {
        if (incomingOrder == null || orderBook == null || tradeHistory == null || executedTrades == null) {
            throw new MatchingException("Matching parameters cannot be null.");
        }

        try {
            while (incomingOrder.getRemainingQuantity() > 0) {
                Order oppositeOrder;
                if (incomingOrder.getSide() == OrderSide.BUY) {
                    oppositeOrder = orderBook.peekBestSell();
                } else {
                    oppositeOrder = orderBook.peekBestBuy();
                }

                if (oppositeOrder == null) {
                    break;
                }

                if (!canMatch(incomingOrder, oppositeOrder)) {
                    break;
                }

                long tradePrice = determineTradePrice(incomingOrder, oppositeOrder);
                long tradeQuantity = Math.min(incomingOrder.getRemainingQuantity(), oppositeOrder.getRemainingQuantity());

                incomingOrder.setRemainingQuantity(incomingOrder.getRemainingQuantity() - tradeQuantity);
                oppositeOrder.setRemainingQuantity(oppositeOrder.getRemainingQuantity() - tradeQuantity);

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

                if (incomingOrder.getRemainingQuantity() == 0) {
                    incomingOrder.setStatus(OrderStatus.FILLED);
                } else {
                    incomingOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
                }

                String buyOrderId = (incomingOrder.getSide() == OrderSide.BUY)
                        ? incomingOrder.getOrderId() : oppositeOrder.getOrderId();
                String sellOrderId = (incomingOrder.getSide() == OrderSide.SELL)
                        ? incomingOrder.getOrderId() : oppositeOrder.getOrderId();

                // Zero-GC: Borrow trade from pool instead of 'new'
                Trade trade = tradePool.borrowObject();
                trade.reset(java.util.UUID.randomUUID().toString(), buyOrderId, sellOrderId, tradePrice, tradeQuantity);
                
                tradeHistory.addTrade(trade);
                executedTrades.add(trade);
            }
        } catch (Exception e) {
            throw new MatchingException("Error occurred during trade matching: " + e.getMessage(), e);
        }
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
