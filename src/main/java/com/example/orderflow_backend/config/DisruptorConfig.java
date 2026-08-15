package com.example.orderflow_backend.config;

import com.exchange.matching.disruptor.MatchingEventHandler;
import com.exchange.matching.disruptor.OrderEvent;
import com.exchange.matching.engine.MatchingEngine;
import com.exchange.matching.history.TradeHistory;
import com.exchange.matching.orderbook.OrderBook;
import com.exchange.matching.validator.OrderValidator;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class DisruptorConfig {

    @Bean
    public OrderBook orderBook() {
        return new OrderBook();
    }

    @Bean
    public TradeHistory tradeHistory() {
        return new TradeHistory();
    }

    @Bean
    public MatchingEngine matchingEngine() {
        return new MatchingEngine();
    }

    @Bean
    public OrderValidator orderValidator() {
        return new OrderValidator();
    }

    @Bean
    public Disruptor<OrderEvent> disruptor(MatchingEngine matchingEngine, OrderBook orderBook, TradeHistory tradeHistory, OrderValidator orderValidator, org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        int bufferSize = 1024 * 64; // Power of 2

        Disruptor<OrderEvent> disruptor = new Disruptor<>(
                OrderEvent::new,
                bufferSize,
                threadFactory,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );

        MatchingEventHandler handler = new MatchingEventHandler(matchingEngine, orderBook, tradeHistory, orderValidator, messagingTemplate);
        disruptor.handleEventsWith(handler);
        disruptor.start();

        return disruptor;
    }

    @Bean
    public RingBuffer<OrderEvent> ringBuffer(Disruptor<OrderEvent> disruptor) {
        return disruptor.getRingBuffer();
    }
}
