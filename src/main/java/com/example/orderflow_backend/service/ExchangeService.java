package com.example.orderflow_backend.service;

import com.exchange.matching.disruptor.OrderEvent;
import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.model.LimitOrder;
import com.exchange.matching.model.MarketOrder;
import com.exchange.matching.model.Order;
import com.exchange.matching.model.Trader;
import com.lmax.disruptor.RingBuffer;
import org.springframework.stereotype.Service;

@Service
public class ExchangeService {

    private final RingBuffer<OrderEvent> ringBuffer;

    public ExchangeService(RingBuffer<OrderEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void submitLimitOrder(String orderId, String traderId, String traderName,
                                 String side, long price, long quantity) {
        Trader trader = new Trader(traderId, traderName);
        OrderSide orderSide = OrderSide.valueOf(side.toUpperCase());
        Order order = new LimitOrder(orderId, trader, orderSide, price, quantity);
        
        long sequence = ringBuffer.next();
        try {
            OrderEvent event = ringBuffer.get(sequence);
            event.set(OrderEvent.EventType.NEW_ORDER, order, null);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    public void submitMarketOrder(String orderId, String traderId, String traderName,
                                  String side, long quantity) {
        Trader trader = new Trader(traderId, traderName);
        OrderSide orderSide = OrderSide.valueOf(side.toUpperCase());
        Order order = new MarketOrder(orderId, trader, orderSide, quantity);

        long sequence = ringBuffer.next();
        try {
            OrderEvent event = ringBuffer.get(sequence);
            event.set(OrderEvent.EventType.NEW_ORDER, order, null);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    public void cancelOrder(String orderId) {
        long sequence = ringBuffer.next();
        try {
            OrderEvent event = ringBuffer.get(sequence);
            event.set(OrderEvent.EventType.CANCEL_ORDER, null, orderId);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
