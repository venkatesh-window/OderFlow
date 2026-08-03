# OrderFlow Matching Engine: Complete Engineering & Architecture Guide

## 1. Introduction
The **OrderFlow Matching Engine** is a high-throughput, low-latency electronic trading order matching module built in **Pure Java 21** using Object-Oriented Programming (OOP) and SOLID design principles. It operates as an autonomous, in-memory matching engine for stock exchange and financial trading applications.

---

## 2. SOLID Design Principles Implementation
- **Single Responsibility Principle (SRP)**:
  - `OrderValidator`: Validates order fields and uniqueness.
  - `OrderBook`: Manages resting order storage and lookups.
  - `MatchingEngine`: Executes price-time priority trade matching.
  - `TradeHistory`: Logs executed trades.
  - `OrderPrinter`: Formats console output.
- **Open/Closed Principle (OCP)**:
  - Abstract `Order` class allows creating new order types (e.g., `StopLimitOrder`, `IcebergOrder`) without modifying core matching logic.
- **Liskov Substitution Principle (LSP)**:
  - All subclasses (`LimitOrder`, `MarketOrder`, `BuyOrder`, `SellOrder`) inherit from `Order` and behave consistently across all queues and validators.
- **Interface Segregation Principle (ISP)**:
  - Clean public methods on `OrderBook`, `TradeHistory`, and `Exchange`.
- **Dependency Inversion Principle (DIP)**:
  - The high-level facade (`Exchange`) depends on abstraction-ready components (`OrderBook`, `MatchingEngine`, `OrderValidator`, `TradeHistory`).

---

## 3. Price-Time Priority Algorithm
1. **Buy Queue Priority (`PriorityQueue`)**:
   - Highest price first.
   - For same price, earlier timestamp first (`LocalDateTime`).
   - Market Buy orders are treated as `Long.MAX_VALUE` price so they poll first.
2. **Sell Queue Priority (`PriorityQueue`)**:
   - Lowest price first.
   - For same price, earlier timestamp first (`LocalDateTime`).
   - Market Sell orders are treated as `Long.MIN_VALUE` price so they poll first.
3. **Execution Price Rule**:
   - Trade price is always determined by the resting order in the book (price improvement for incoming order).

---

## 4. Integration Guide for Member 2 (Spring Boot Developer)

### 4.1 Step 1: Wrap in a Thread-Safe Spring Service
Because the core matching engine is pure Java, Member 2 should wrap `com.exchange.matching.engine.Exchange` in a Spring `@Service` using a `java.util.concurrent.locks.ReentrantLock(true)` (fair lock) to guarantee thread safety:

```java
@Service
public class MatchingEngineService {
    private final Exchange exchange = new Exchange();
    private final ReentrantLock lock = new ReentrantLock(true);

    public List<Trade> placeOrder(Order order) {
        lock.lock();
        try {
            return exchange.addOrder(order);
        } finally {
            lock.unlock();
        }
    }

    public Order cancelOrder(String orderId) {
        lock.lock();
        try {
            return exchange.cancelOrder(orderId);
        } finally {
            lock.unlock();
        }
    }
}
```

### 4.2 Step 2: REST API Controllers
Create a `@RestController` in `com.example.orderflow_backend.controller` that maps HTTP POST/DELETE requests to the `MatchingEngineService`.

---

## 5. Summary of Test Coverage (`MatchingEngineTest.java`)
1. **testCompleteMatch**: 100 shares @ 50 matches 100 shares @ 50 completely.
2. **testPartialMatch**: Buy 100 shares @ 50 matches Sell 60 shares @ 50; 40 shares remain resting.
3. **testMultipleBuyOrders**: Sell order matches against multiple buy orders in descending price order.
4. **testMultipleSellOrders**: Buy order matches against multiple sell orders in ascending price order.
5. **testFifoVerification**: Same price orders execute in strict timestamp order.
6. **testMarketBuy**: Market buy matches lowest sell.
7. **testMarketSell**: Market sell matches highest buy.
8. **testCancelOrder**: Verifies `OrderStatus.CANCELLED` and removal from book.
9. **testModifyOrder**: Modifying price/quantity resets timestamp and attempts immediate matching.
10. **testDuplicateOrder**: Throws `DuplicateOrderException`.
11. **testInvalidPrice**: Throws `InvalidOrderException` for negative/zero limit price.
12. **testInvalidQuantity**: Throws `InvalidOrderException` for zero/negative quantity.
13. **testOrderNotFound**: Throws `OrderNotFoundException` when cancelling/modifying missing ID.
