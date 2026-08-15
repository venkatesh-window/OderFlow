# OrderFlow: High-Throughput Matching Engine & Market Data Gateway
## Module: Pure Java Order Matching Engine

---

## 1. Executive Summary & Architecture Overview

The **OrderFlow Matching Engine** is a high-performance, low-latency electronic trading order matching module built in **Pure Java 21** using **Object-Oriented Programming (OOP)** and **SOLID design principles**. It emulates the core execution logic of a real stock exchange (such as NYSE or NASDAQ).

The engine maintains dual order books (Buy and Sell) and matches incoming limit and market orders using strict **Price-Time Priority (FIFO)** rules. It is designed as an autonomous, thread-safe-ready Java module that can be easily integrated into Spring Boot REST APIs, Kafka messaging pipelines, or WebSocket gateways by a secondary developer.

```
       +-------------------------------------------------------------+
       |                        EXCHANGE FACADE                      |
       |  +-------------------------------------------------------+  |
       |  |                   OrderValidator                      |  |
       |  +---------------------------+---------------------------+  |
       |                              |                              |
       |                              v                              |
       |  +-------------------------------------------------------+  |
       |  |                    MatchingEngine                     |  |
       |  |  +---------------------+     +---------------------+  |  |
       |  |  |     BUY BOOK        |     |     SELL BOOK       |  |  |
       |  |  |  (PriorityQueue)    |     |  (PriorityQueue)    |  |  |
       |  |  +---------------------+     +---------------------+  |  |
       |  +---------------------------+---------------------------+  |
       |                              |                              |
       |                              v                              |
       |  +-------------------------------------------------------+  |
       |  |                     TradeHistory                      |  |
       |  +-------------------------------------------------------+  |
       +-------------------------------------------------------------+
```

---

## 2. SOLID Design Principles & Design Patterns Implemented

| Principle / Pattern | Implementation in Code |
| :--- | :--- |
| **Single Responsibility Principle (SRP)** | Each class has exactly one responsibility:<br>• `OrderValidator`: Only validates order parameters and duplicate IDs.<br>• `OrderBook`: Only manages data structures and O(1)/O(log n) storage of resting orders.<br>• `MatchingEngine`: Only implements the core Price-Time priority matching algorithm.<br>• `TradeHistory`: Only records and formats executed trade logs.<br>• `OrderPrinter`: Only handles formatting and console display. |
| **Open/Closed Principle (OCP)** | The `Order` hierarchy is open for extension but closed for modification. New order types (e.g., Stop-Loss, Iceberg) can inherit from `Order` without altering `MatchingEngine` or `OrderBook`. |
| **Liskov Substitution Principle (LSP)** | `LimitOrder`, `MarketOrder`, `BuyOrder`, and `SellOrder` can be used interchangeably anywhere `Order` is expected without breaking functionality or invariants. |
| **Interface Segregation Principle (ISP)** | Clean, narrow method contracts are provided on `OrderBook`, `TradeHistory`, and `Exchange` so callers do not depend on methods they do not use. |
| **Dependency Inversion Principle (DIP)** | High-level orchestration (`Exchange`) depends on modular components (`OrderBook`, `MatchingEngine`, `OrderValidator`, `TradeHistory`) injected via constructor overloading. |
| **Facade Pattern** | The `Exchange` class acts as a unified Facade, shielding API consumers from internal queue manipulation, validation steps, and matching engine complexity. |
| **Strategy Pattern** | `OrderComparator` provides pluggable `Comparator<Order>` strategies for Price-Time priority sorting on Buy and Sell queues. |

---

## 3. Price-Time Priority (FIFO) Matching Rules

### 3.1 Comparator Logic (`OrderComparator.java`)
1. **BUY Order Book Priority (`PriorityQueue<Order>`)**:
   - **Primary Key (Price - Descending)**: Highest price has highest priority.
   - **Market Order Treatment**: Market Buy orders are assigned `Long.MAX_VALUE` so they are always polled first.
   - **Secondary Key (Timestamp - Ascending)**: If prices are equal, the order with the earlier `LocalDateTime timestamp` is executed first (Strict FIFO).

2. **SELL Order Book Priority (`PriorityQueue<Order>`)**:
   - **Primary Key (Price - Ascending)**: Lowest price has highest priority.
   - **Market Order Treatment**: Market Sell orders are assigned `Long.MIN_VALUE` so they are always polled first.
   - **Secondary Key (Timestamp - Ascending)**: If prices are equal, the earlier `LocalDateTime timestamp` is executed first.

### 3.2 Price Crossing & Execution Rules
- **Limit Buy against Limit Sell**: A match occurs only if `Buy Price >= Sell Price`.
- **Limit Sell against Limit Buy**: A match occurs only if `Sell Price <= Buy Price`.
- **Market Buy / Market Sell**: Always matches against the best available opposite resting order.
- **Trade Price Determination**: The executed trade price is **always determined by the resting order's price** in the order book (price improvement for the incoming order).
- **Trade Quantity Determination**: `Trade Quantity = min(Incoming Remaining Quantity, Resting Remaining Quantity)`.
- **Status Progression**:
  - `NEW` $\rightarrow$ `PARTIALLY_FILLED` (when quantity is partially matched).
  - `NEW` / `PARTIALLY_FILLED` $\rightarrow$ `FILLED` (when `remainingQuantity == 0`).
  - `NEW` / `PARTIALLY_FILLED` $\rightarrow$ `CANCELLED` (when cancelled by ID).

---

## 4. Package & Folder Structure

```
c:\Users\gsven\OrderFlow\src
├───main
│   └───java
│       └───com
│           └───exchange
│               └───matching
│                   ├───enums
│                   │       OrderSide.java
│                   │       OrderStatus.java
│                   │       OrderType.java
│                   ├───exception
│                   │       DuplicateOrderException.java
│                   │       InvalidOrderException.java
│                   │       MatchingException.java
│                   │       OrderNotFoundException.java
│                   ├───model
│                   │       BuyOrder.java
│                   │       LimitOrder.java
│                   │       MarketOrder.java
│                   │       Order.java
│                   │       SellOrder.java
│                   │       Trade.java
│                   │       Trader.java
│                   ├───orderbook
│                   │       OrderBook.java
│                   ├───validator
│                   │       OrderValidator.java
│                   ├───engine
│                   │       MatchingEngine.java
│                   │       Exchange.java
│                   ├───history
│                   │       TradeHistory.java
│                   └───util
│                           OrderComparator.java
│                           OrderPrinter.java
└───test
    └───java
        └───com
            └───exchange
                └───matching
                    └───engine
                            MatchingEngineTest.java
```

---

## 5. Integration Guide for Member 2 (Spring Boot REST API Developer)

This section explains how **Member 2** can integrate this Pure Java Matching Engine into a **Spring Boot 3 / Java 21 REST API** application.

### 5.1 Architecture Layering
The matching engine is designed as the **Domain Core / Engine Layer**. It should be wrapped by Spring Boot's **Service Layer**, which manages synchronization, persistence, and DTO conversion.

```
+-------------------------------------------------------------------------+
|                  REST CONTROLLERS (Spring MVC / WebFlux)               |
|      POST /api/v1/orders  |  DELETE /api/v1/orders/{id}                 |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                 EXCHANGE SERVICE LAYER (Spring @Service)                |
|  • Maintains singleton instance of com.exchange.matching.engine.Exchange|
|  • Implements Thread-Safety via java.util.concurrent.locks.ReentrantLock|
|  • Translates API Request DTOs <--> Pure Java Models                    |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                      PURE JAVA MATCHING ENGINE                          |
|                       Exchange.addOrder(Order)                          |
+-------------------------------------------------------------------------+
```

### 5.2 Thread-Safe Service Implementation Example (`ExchangeService.java`)
Member 2 should create a Spring `@Service` bean that wraps `Exchange` with a `ReentrantLock` to ensure atomic order submission in a multi-threaded web server environment:

```java
package com.example.orderflow_backend.service;

import com.exchange.matching.engine.Exchange;
import com.exchange.matching.model.*;
import com.exchange.matching.enums.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ExchangeService {

    private final Exchange exchange = new Exchange();
    private final ReentrantLock lock = new ReentrantLock(true); // Fair lock for FIFO ordering across threads

    public List<Trade> submitLimitOrder(String orderId, String traderId, String traderName,
                                        String side, long price, long quantity) {
        lock.lock();
        try {
            Trader trader = new Trader(traderId, traderName);
            OrderSide orderSide = OrderSide.valueOf(side.toUpperCase());
            Order order = new LimitOrder(orderId, trader, orderSide, price, quantity);
            return exchange.addOrder(order);
        } finally {
            lock.unlock();
        }
    }

    public List<Trade> submitMarketOrder(String orderId, String traderId, String traderName,
                                         String side, long quantity) {
        lock.lock();
        try {
            Trader trader = new Trader(traderId, traderName);
            OrderSide orderSide = OrderSide.valueOf(side.toUpperCase());
            Order order = new MarketOrder(orderId, trader, orderSide, quantity);
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

    public List<Trade> getTradeHistory() {
        lock.lock();
        try {
            return exchange.getTradeHistory().getTrades();
        } finally {
            lock.unlock();
        }
    }
}
```

### 5.3 REST Controller Implementation Example (`OrderController.java`)
Member 2 can expose endpoints for order submission and cancellation:

```java
package com.example.orderflow_backend.controller;

import com.example.orderflow_backend.service.ExchangeService;
import com.exchange.matching.model.Order;
import com.exchange.matching.model.Trade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final ExchangeService exchangeService;

    public OrderController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping("/limit")
    public ResponseEntity<List<Trade>> createLimitOrder(@RequestParam String orderId,
                                                        @RequestParam String traderId,
                                                        @RequestParam String traderName,
                                                        @RequestParam String side,
                                                        @RequestParam long price,
                                                        @RequestParam long quantity) {
        List<Trade> trades = exchangeService.submitLimitOrder(orderId, traderId, traderName, side, price, quantity);
        return ResponseEntity.ok(trades);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable String orderId) {
        Order cancelled = exchangeService.cancelOrder(orderId);
        return ResponseEntity.ok(cancelled);
    }
}
```

---

## 6. Running Unit Tests

To compile and verify all 13 test cases:
```bash
mvn test
```
All tests in `com.exchange.matching.engine.MatchingEngineTest` execute autonomously without requiring a Spring application context or external database.

---

## 7. How to Run the Application

The OrderFlow application consists of a high-performance Java backend and a modern React/Vite frontend. You must start both for the application to function end-to-end.

### 7.1 Start the Backend (Spring Boot)
Open a terminal in the root directory (`c:\Users\gsven\OrderFlow`) and run:
```bash
# On Windows
.\mvnw spring-boot:run

# On Mac/Linux
./mvnw spring-boot:run
```
This will compile the Java engine and start the REST API & WebSocket server on `http://localhost:8080`.

### 7.2 Start the Frontend (React & Vite)
Open a **new** terminal, navigate into the `frontend` folder, and start the development server:
```bash
cd frontend

# Install dependencies (only required the first time)
npm install

# Start the frontend UI
npm run dev
```
This will start the frontend interface. Look at your terminal output for the local URL (usually `http://localhost:5173`) and open it in your web browser to start trading!
