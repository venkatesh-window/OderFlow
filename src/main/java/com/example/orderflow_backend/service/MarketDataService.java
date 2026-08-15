package com.example.orderflow_backend.service;

import com.exchange.matching.model.Order;
import com.exchange.matching.orderbook.OrderBook;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class MarketDataService {

    private final OrderBook orderBook;
    private final SimpMessagingTemplate messagingTemplate;

    public MarketDataService(OrderBook orderBook, SimpMessagingTemplate messagingTemplate) {
        this.orderBook = orderBook;
        this.messagingTemplate = messagingTemplate;
    }

    // Broadcast L2 Order Book Depth every 100ms
    @Scheduled(fixedRate = 100)
    public void broadcastOrderBook() {
        if (orderBook == null) return;

        List<Order> buys = orderBook.getBuyOrders();
        List<Order> sells = orderBook.getSellOrders();

        Map<Long, Long> bids = aggregateDepth(buys);
        Map<Long, Long> asks = aggregateDepth(sells);

        OrderBookSnapshot snapshot = new OrderBookSnapshot(bids, asks);
        messagingTemplate.convertAndSend("/topic/orderbook", snapshot);
    }

    private Map<Long, Long> aggregateDepth(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getPrice,
                        Collectors.summingLong(Order::getRemainingQuantity)
                ));
    }

    public static class OrderBookSnapshot {
        public Map<Long, Long> bids;
        public Map<Long, Long> asks;

        public OrderBookSnapshot(Map<Long, Long> bids, Map<Long, Long> asks) {
            this.bids = bids;
            this.asks = asks;
        }
    }
}
