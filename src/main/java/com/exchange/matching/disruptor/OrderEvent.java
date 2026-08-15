package com.exchange.matching.disruptor;

import com.exchange.matching.model.Order;
import com.exchange.matching.model.Trade;
import java.util.List;

/**
 * Mutable event class used to pass order data through the LMAX Disruptor RingBuffer.
 * This object is pre-allocated and reused to avoid Garbage Collection.
 */
public class OrderEvent {

    /**
     * Constructs a new OrderEvent.
     */
    public OrderEvent() {
    }

    /**
     * Defines the type of order event to be processed.
     */
    public enum EventType {
        /** Represents a new order submission. */
        NEW_ORDER, 
        /** Represents a request to cancel an existing order. */
        CANCEL_ORDER
    }

    private EventType eventType;
    private Order order;
    private String orderIdToCancel;
    private final List<Trade> resultTrades = new java.util.ArrayList<>(64);
    private Order cancelledOrder;

    /**
     * Populates the event data for reuse.
     *
     * @param eventType       the type of event
     * @param order           the incoming order, or null if canceling
     * @param orderIdToCancel the ID of the order to cancel, or null if new order
     */
    public void set(EventType eventType, Order order, String orderIdToCancel) {
        this.eventType = eventType;
        this.order = order;
        this.orderIdToCancel = orderIdToCancel;
        this.resultTrades.clear();
        this.cancelledOrder = null;
    }

    /**
     * Clears the event data before it is returned to the RingBuffer pool.
     */
    public void clear() {
        this.eventType = null;
        this.order = null;
        this.orderIdToCancel = null;
        this.resultTrades.clear();
        this.cancelledOrder = null;
    }

    /** @return the type of the event */
    public EventType getEventType() { return eventType; }
    
    /** @return the order associated with the event */
    public Order getOrder() { return order; }
    
    /** @return the ID of the order to cancel */
    public String getOrderIdToCancel() { return orderIdToCancel; }
    
    /** @return the list of trades generated from processing this event */
    public List<Trade> getResultTrades() { return resultTrades; }
    
    /** @param cancelledOrder sets the order that was successfully canceled */
    public void setCancelledOrder(Order cancelledOrder) { this.cancelledOrder = cancelledOrder; }
    
    /** @return the order that was successfully canceled */
    public Order getCancelledOrder() { return cancelledOrder; }
}
