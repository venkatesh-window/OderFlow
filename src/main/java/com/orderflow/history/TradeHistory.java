package com.orderflow.history;

import com.orderflow.model.Trade;
import com.orderflow.util.OrderPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and manages the history of executed trades within the matching engine.
 */
public class TradeHistory {
    private final List<Trade> trades;

    public TradeHistory() {
        this.trades = new ArrayList<>();
    }

    /**
     * Records a newly executed trade in the history log.
     *
     * @param trade the executed trade
     */
    public void addTrade(Trade trade) {
        if (trade != null) {
            trades.add(trade);
        }
    }

    /**
     * Returns an unmodifiable view of all executed trades in chronological order.
     *
     * @return list of executed trades
     */
    public List<Trade> getTrades() {
        return Collections.unmodifiableList(trades);
    }

    /**
     * Prints the complete executed trade history to the console.
     */
    public void printTradeHistory() {
        OrderPrinter.printTradeHistory(trades);
    }

    public int size() {
        return trades.size();
    }

    public boolean isEmpty() {
        return trades.isEmpty();
    }

    public void clear() {
        trades.clear();
    }
}
