package com.orderflow.engine;

import com.orderflow.enums.OrderStatus;
import com.orderflow.enums.OrderType;
import com.orderflow.exception.DuplicateOrderException;
import com.orderflow.exception.InvalidOrderException;
import com.orderflow.exception.OrderNotFoundException;
import com.orderflow.model.BuyOrder;
import com.orderflow.model.LimitOrder;
import com.orderflow.model.MarketOrder;
import com.orderflow.model.Order;
import com.orderflow.model.SellOrder;
import com.orderflow.model.Trade;
import com.orderflow.model.Trader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Complete JUnit 5 test suite covering all required test cases for the Matching Engine.
 */
class MatchingEngineTest {

    private Exchange exchange;
    private Trader traderA;
    private Trader traderB;
    private Trader traderC;

    @BeforeEach
    void setUp() {
        exchange = new Exchange();
        traderA = new Trader("T1", "Alice");
        traderB = new Trader("T2", "Bob");
        traderC = new Trader("T3", "Charlie");
    }

    @Test
    @DisplayName("Complete Match: 100 shares @ 50 matches 100 shares @ 50 completely")
    void testCompleteMatch() {
        Order sell = new SellOrder("S1", traderA, 50L, 100L);
        Order buy = new BuyOrder("B1", traderB, 50L, 100L);

        exchange.addOrder(sell);
        List<Trade> trades = exchange.addOrder(buy);

        assertEquals(1, trades.size());
        Trade trade = trades.get(0);
        assertEquals(50L, trade.getPrice());
        assertEquals(100L, trade.getQuantity());
        assertEquals("B1", trade.getBuyOrderId());
        assertEquals("S1", trade.getSellOrderId());

        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(OrderStatus.FILLED, buy.getStatus());
        assertTrue(exchange.getOrderBook().isEmpty());
    }

    @Test
    @DisplayName("Partial Match: Buy 100 shares @ 50 matches Sell 60 shares @ 50")
    void testPartialMatch() {
        Order sell = new SellOrder("S1", traderA, 50L, 60L);
        Order buy = new BuyOrder("B1", traderB, 50L, 100L);

        exchange.addOrder(sell);
        List<Trade> trades = exchange.addOrder(buy);

        assertEquals(1, trades.size());
        assertEquals(60L, trades.get(0).getQuantity());

        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(OrderStatus.PARTIALLY_FILLED, buy.getStatus());
        assertEquals(40L, buy.getRemainingQuantity());
        assertEquals(1, exchange.getOrderBook().getBuyOrders().size());
        assertEquals(0, exchange.getOrderBook().getSellOrders().size());
    }

    @Test
    @DisplayName("Multiple Buy Orders: Sell order matches against multiple buy orders in price-time order")
    void testMultipleBuyOrders() {
        Order buy1 = new BuyOrder("B1", traderA, 100L, 50L);
        Order buy2 = new BuyOrder("B2", traderB, 105L, 30L); // Higher price -> higher priority
        Order buy3 = new BuyOrder("B3", traderC, 95L, 50L);

        exchange.addOrder(buy1);
        exchange.addOrder(buy2);
        exchange.addOrder(buy3);

        Order sell = new SellOrder("S1", traderB, 98L, 60L);
        List<Trade> trades = exchange.addOrder(sell);

        assertEquals(2, trades.size());
        // First trade should match B2 (price 105) for 30 shares
        assertEquals("B2", trades.get(0).getBuyOrderId());
        assertEquals(105L, trades.get(0).getPrice());
        assertEquals(30L, trades.get(0).getQuantity());

        // Second trade should match B1 (price 100) for remaining 30 shares
        assertEquals("B1", trades.get(1).getBuyOrderId());
        assertEquals(100L, trades.get(1).getPrice());
        assertEquals(30L, trades.get(1).getQuantity());

        assertEquals(20L, buy1.getRemainingQuantity());
        assertEquals(OrderStatus.PARTIALLY_FILLED, buy1.getStatus());
        assertEquals(OrderStatus.FILLED, buy2.getStatus());
        assertEquals(OrderStatus.NEW, buy3.getStatus());
    }

    @Test
    @DisplayName("Multiple Sell Orders: Buy order matches against multiple sell orders in lowest price order")
    void testMultipleSellOrders() {
        Order sell1 = new SellOrder("S1", traderA, 50L, 40L);
        Order sell2 = new SellOrder("S2", traderB, 48L, 30L); // Lowest price -> highest priority
        Order sell3 = new SellOrder("S3", traderC, 52L, 50L);

        exchange.addOrder(sell1);
        exchange.addOrder(sell2);
        exchange.addOrder(sell3);

        Order buy = new BuyOrder("B1", traderA, 50L, 60L);
        List<Trade> trades = exchange.addOrder(buy);

        assertEquals(2, trades.size());
        assertEquals("S2", trades.get(0).getSellOrderId());
        assertEquals(48L, trades.get(0).getPrice());
        assertEquals(30L, trades.get(0).getQuantity());

        assertEquals("S1", trades.get(1).getSellOrderId());
        assertEquals(50L, trades.get(1).getPrice());
        assertEquals(30L, trades.get(1).getQuantity());
    }

    @Test
    @DisplayName("FIFO Verification: Orders at the same price match in strict timestamp order")
    void testFifoVerification() {
        LocalDateTime now = LocalDateTime.now();
        Order buy1 = new BuyOrder("B1", traderA, OrderType.LIMIT, 100L, 50L, now);
        Order buy2 = new BuyOrder("B2", traderB, OrderType.LIMIT, 100L, 50L, now.plusSeconds(1));
        Order buy3 = new BuyOrder("B3", traderC, OrderType.LIMIT, 100L, 50L, now.plusSeconds(2));

        exchange.addOrder(buy1);
        exchange.addOrder(buy2);
        exchange.addOrder(buy3);

        Order sell = new SellOrder("S1", traderB, 100L, 60L);
        List<Trade> trades = exchange.addOrder(sell);

        assertEquals(2, trades.size());
        assertEquals("B1", trades.get(0).getBuyOrderId());
        assertEquals(50L, trades.get(0).getQuantity());

        assertEquals("B2", trades.get(1).getBuyOrderId());
        assertEquals(10L, trades.get(1).getQuantity());
    }

    @Test
    @DisplayName("Market Buy: Market Buy order matches against lowest available sell order")
    void testMarketBuy() {
        Order sell1 = new SellOrder("S1", traderA, 102L, 50L);
        Order sell2 = new SellOrder("S2", traderB, 99L, 50L);

        exchange.addOrder(sell1);
        exchange.addOrder(sell2);

        Order marketBuy = new MarketOrder("MB1", traderC, com.orderflow.enums.OrderSide.BUY, 30L);
        List<Trade> trades = exchange.addOrder(marketBuy);

        assertEquals(1, trades.size());
        assertEquals("S2", trades.get(0).getSellOrderId());
        assertEquals(99L, trades.get(0).getPrice());
        assertEquals(30L, trades.get(0).getQuantity());
    }

    @Test
    @DisplayName("Market Sell: Market Sell order matches against highest available buy order")
    void testMarketSell() {
        Order buy1 = new BuyOrder("B1", traderA, 100L, 50L);
        Order buy2 = new BuyOrder("B2", traderB, 105L, 50L);

        exchange.addOrder(buy1);
        exchange.addOrder(buy2);

        Order marketSell = new MarketOrder("MS1", traderC, com.orderflow.enums.OrderSide.SELL, 40L);
        List<Trade> trades = exchange.addOrder(marketSell);

        assertEquals(1, trades.size());
        assertEquals("B2", trades.get(0).getBuyOrderId());
        assertEquals(105L, trades.get(0).getPrice());
        assertEquals(40L, trades.get(0).getQuantity());
    }

    @Test
    @DisplayName("Cancel Order: Existing order in the book can be cancelled")
    void testCancelOrder() {
        Order buy = new BuyOrder("B1", traderA, 100L, 50L);
        exchange.addOrder(buy);
        assertEquals(1, exchange.getOrderBook().getBuyOrders().size());

        Order cancelled = exchange.cancelOrder("B1");
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertTrue(exchange.getOrderBook().isEmpty());
    }

    @Test
    @DisplayName("Modify Order: Modifying order price/quantity updates order and matches if possible")
    void testModifyOrder() {
        Order sell = new SellOrder("S1", traderA, 100L, 50L);
        exchange.addOrder(sell);

        Order buy = new BuyOrder("B1", traderB, 90L, 50L);
        exchange.addOrder(buy);

        // Modify Buy price from 90 to 100 -> should match S1
        List<Trade> trades = exchange.modifyOrder("B1", 100L, 50L);
        assertEquals(1, trades.size());
        assertEquals("S1", trades.get(0).getSellOrderId());
        assertEquals("B1", trades.get(0).getBuyOrderId());
        assertTrue(exchange.getOrderBook().isEmpty());
    }

    @Test
    @DisplayName("Duplicate Order: Submitting an order with a duplicate ID throws DuplicateOrderException")
    void testDuplicateOrder() {
        Order buy1 = new BuyOrder("B1", traderA, 100L, 50L);
        exchange.addOrder(buy1);

        Order buy2 = new BuyOrder("B1", traderB, 100L, 10L);
        assertThrows(DuplicateOrderException.class, () -> exchange.addOrder(buy2));
    }

    @Test
    @DisplayName("Invalid Price: Submitting a limit order with negative or zero price throws InvalidOrderException")
    void testInvalidPrice() {
        Order negativePriceOrder = new BuyOrder("B1", traderA, -10L, 50L);
        assertThrows(InvalidOrderException.class, () -> exchange.addOrder(negativePriceOrder));

        Order zeroPriceLimitOrder = new BuyOrder("B2", traderA, 0L, 50L);
        assertThrows(InvalidOrderException.class, () -> exchange.addOrder(zeroPriceLimitOrder));
    }

    @Test
    @DisplayName("Invalid Quantity: Submitting an order with zero or negative quantity throws InvalidOrderException")
    void testInvalidQuantity() {
        Order zeroQtyOrder = new BuyOrder("B1", traderA, 100L, 0L);
        assertThrows(InvalidOrderException.class, () -> exchange.addOrder(zeroQtyOrder));

        Order negativeQtyOrder = new BuyOrder("B2", traderA, 100L, -5L);
        assertThrows(InvalidOrderException.class, () -> exchange.addOrder(negativeQtyOrder));
    }

    @Test
    @DisplayName("Order Not Found: Cancelling or modifying a non-existent order throws OrderNotFoundException")
    void testOrderNotFound() {
        assertThrows(OrderNotFoundException.class, () -> exchange.cancelOrder("NON_EXISTENT_ID"));
        assertThrows(OrderNotFoundException.class, () -> exchange.modifyOrder("NON_EXISTENT_ID", 100L, 50L));
    }
}
