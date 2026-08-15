package com.exchange.matching.disruptor;

import com.exchange.matching.engine.MatchingEngine;
import com.exchange.matching.exception.DuplicateOrderException;
import com.exchange.matching.exception.InvalidOrderException;
import com.exchange.matching.exception.OrderNotFoundException;
import com.exchange.matching.history.TradeHistory;
import com.exchange.matching.model.Order;
import com.exchange.matching.model.Trade;
import com.exchange.matching.orderbook.OrderBook;
import com.exchange.matching.validator.OrderValidator;
import com.lmax.disruptor.EventHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Collections;
import java.util.List;

/**
 * Handles incoming order events from the LMAX Disruptor RingBuffer.
 * This event handler routes orders to the matching engine and broadcasts
 * executed trades via WebSockets.
 */
public class MatchingEventHandler implements EventHandler<OrderEvent> {

    private final MatchingEngine matchingEngine;
    private final OrderBook orderBook;
    private final TradeHistory tradeHistory;
    private final OrderValidator orderValidator;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructs a new MatchingEventHandler.
     *
     * @param matchingEngine    the engine that executes matches
     * @param orderBook         the central order book
     * @param tradeHistory      the history of all executed trades
     * @param orderValidator    the validator for incoming orders
     * @param messagingTemplate the template used to send WebSocket messages
     */
    public MatchingEventHandler(MatchingEngine matchingEngine, OrderBook orderBook, TradeHistory tradeHistory, OrderValidator orderValidator, SimpMessagingTemplate messagingTemplate) {
        this.matchingEngine = matchingEngine;
        this.orderBook = orderBook;
        this.tradeHistory = tradeHistory;
        this.orderValidator = orderValidator;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            if (event.getEventType() == OrderEvent.EventType.NEW_ORDER) {
                Order order = event.getOrder();
                orderValidator.validate(order, orderBook);
                orderBook.addOrder(order);
                List<Trade> trades = event.getResultTrades();
                matchingEngine.match(order, orderBook, tradeHistory, trades);
                
                // Broadcast trades
                if (!trades.isEmpty() && messagingTemplate != null) {
                    messagingTemplate.convertAndSend("/topic/trades", trades);
                }
            } else if (event.getEventType() == OrderEvent.EventType.CANCEL_ORDER) {
                String orderId = event.getOrderIdToCancel();
                Order cancelledOrder = orderBook.cancelOrder(orderId);
                event.setCancelledOrder(cancelledOrder);
            }
        } catch (DuplicateOrderException | InvalidOrderException | OrderNotFoundException e) {
            // Log or handle validation/not found exceptions gracefully
            System.err.println("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Critical matching error: " + e.getMessage());
        }
    }
}
