package com.exchange.matching.engine;

import com.exchange.matching.enums.OrderSide;
import com.exchange.matching.history.TradeHistory;
import com.exchange.matching.model.LimitOrder;
import com.exchange.matching.model.Order;
import com.exchange.matching.model.Trader;
import com.exchange.matching.model.Trade;
import com.exchange.matching.orderbook.OrderBook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LatencyBenchmark {

    public static void main(String[] args) {
        System.out.println("Starting Latency Audit Benchmark...");
        
        MatchingEngine engine = new MatchingEngine();
        OrderBook orderBook = new OrderBook();
        TradeHistory tradeHistory = new TradeHistory();
        Trader trader = new Trader("TRD1", "Test Trader");
        Random random = new Random(42);

        int warmupCount = 100_000;
        int testCount = 1_000_000;
        long[] latencies = new long[testCount];

        // 1. Warmup (Allow JIT compiler to optimize)
        System.out.println("Warming up with " + warmupCount + " orders...");
        List<Trade> dummyList = new ArrayList<>(10);
        for (int i = 0; i < warmupCount; i++) {
            boolean isBuy = random.nextBoolean();
            long price = 100 + random.nextInt(20);
            Order order = new LimitOrder("W-" + i, trader, isBuy ? OrderSide.BUY : OrderSide.SELL, price, 10);
            orderBook.addOrder(order);
            dummyList.clear();
            engine.match(order, orderBook, tradeHistory, dummyList);
        }

        orderBook.clear();
        
        System.gc();
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // 2. Benchmark
        System.out.println("Running benchmark with " + testCount + " orders...");
        long totalStartTime = System.nanoTime();
        
        for (int i = 0; i < testCount; i++) {
            boolean isBuy = random.nextBoolean();
            long price = 100 + random.nextInt(20);
            Order order = new LimitOrder("O-" + i, trader, isBuy ? OrderSide.BUY : OrderSide.SELL, price, 10);
            
            long start = System.nanoTime();
            orderBook.addOrder(order);
            dummyList.clear();
            engine.match(order, orderBook, tradeHistory, dummyList);
            long end = System.nanoTime();
            
            latencies[i] = (end - start);
        }
        
        long totalEndTime = System.nanoTime();

        // 3. Calculate Percentiles
        Arrays.sort(latencies);
        
        long p50 = latencies[(int) (testCount * 0.50)];
        long p90 = latencies[(int) (testCount * 0.90)];
        long p99 = latencies[(int) (testCount * 0.99)];
        long p999 = latencies[(int) (testCount * 0.999)];
        long max = latencies[testCount - 1];
        
        double totalTimeSecs = (totalEndTime - totalStartTime) / 1_000_000_000.0;
        double ordersPerSecond = testCount / totalTimeSecs;

        System.out.println("\n--- BENCHMARK RESULTS ---");
        System.out.printf("Total Orders Processed: %,d\n", testCount);
        System.out.printf("Total Time Taken: %.3f seconds\n", totalTimeSecs);
        System.out.printf("Throughput: %,.0f orders/second\n", ordersPerSecond);
        
        System.out.println("\n--- LATENCY (Microseconds) ---");
        System.out.printf("50th Percentile (Median): %.2f us\n", p50 / 1000.0);
        System.out.printf("90th Percentile:          %.2f us\n", p90 / 1000.0);
        System.out.printf("99th Percentile:          %.2f us\n", p99 / 1000.0);
        System.out.printf("99.9th Percentile:        %.2f us\n", p999 / 1000.0);
        System.out.printf("Max Latency:              %.2f us\n", max / 1000.0);
        
        if (p99 / 1000.0 < 100 && ordersPerSecond >= 100_000) {
            System.out.println("\n[SUCCESS] Latency Audit Passed! Engine can process >100k ops/sec with p99 < 100us.");
        } else {
            System.out.println("\n[FAILED] Engine did not meet the latency audit requirements.");
        }
    }
}
