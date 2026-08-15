package com.example.orderflow_backend.controller;

import com.example.orderflow_backend.service.ExchangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling order-related HTTP requests.
 */
@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(originPatterns = "*") // Allow frontend
public class OrderController {

    private final ExchangeService exchangeService;

    /**
     * Constructs a new OrderController with the required service.
     *
     * @param exchangeService the service to handle business logic
     */
    public OrderController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    /**
     * Endpoint to submit a limit order.
     *
     * @param orderId    the unique order ID
     * @param traderId   the trader's ID
     * @param traderName the trader's name
     * @param side       the side of the order (BUY/SELL)
     * @param price      the limit price
     * @param quantity   the order quantity
     * @return an HTTP response indicating acceptance
     */
    @PostMapping("/limit")
    public ResponseEntity<Void> createLimitOrder(@RequestParam String orderId,
                                                 @RequestParam String traderId,
                                                 @RequestParam String traderName,
                                                 @RequestParam String side,
                                                 @RequestParam long price,
                                                 @RequestParam long quantity) {
        exchangeService.submitLimitOrder(orderId, traderId, traderName, side, price, quantity);
        return ResponseEntity.accepted().build();
    }

    /**
     * Endpoint to submit a market order.
     *
     * @param orderId    the unique order ID
     * @param traderId   the trader's ID
     * @param traderName the trader's name
     * @param side       the side of the order (BUY/SELL)
     * @param quantity   the order quantity
     * @return an HTTP response indicating acceptance
     */
    @PostMapping("/market")
    public ResponseEntity<Void> createMarketOrder(@RequestParam String orderId,
                                                  @RequestParam String traderId,
                                                  @RequestParam String traderName,
                                                  @RequestParam String side,
                                                  @RequestParam long quantity) {
        exchangeService.submitMarketOrder(orderId, traderId, traderName, side, quantity);
        return ResponseEntity.accepted().build();
    }

    /**
     * Endpoint to cancel an existing order.
     *
     * @param orderId the ID of the order to cancel
     * @return an HTTP response indicating acceptance
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId) {
        exchangeService.cancelOrder(orderId);
        return ResponseEntity.accepted().build();
    }
}
