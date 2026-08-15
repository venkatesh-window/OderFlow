import { useMemo } from 'react';
import type { OrderBookSnapshot } from '../services/WebSocketClient';
import './OrderBook.css';

interface OrderBookProps {
    snapshot: OrderBookSnapshot | null;
}

interface PriceLevel {
    price: number;
    quantity: number;
    total: number;
    depthPercent: number;
}

export function OrderBook({ snapshot }: OrderBookProps) {
    const { bids, asks } = useMemo(() => {
        if (!snapshot) return { bids: [], asks: [] };

        const processSide = (data: Record<string, number>, isAscending: boolean): PriceLevel[] => {
            const sortedPrices = Object.keys(data)
                .map(Number)
                .sort((a, b) => isAscending ? a - b : b - a)
                .slice(0, 20); // Show top 20 levels

            let total = 0;
            return sortedPrices.map(price => {
                total += data[price];
                return {
                    price,
                    quantity: data[price],
                    total,
                    depthPercent: 0 // Will calculate after finding maxTotal
                };
            });
        };

        const sortedBids = processSide(snapshot.bids, false); // Bids descending (highest first)
        const sortedAsks = processSide(snapshot.asks, true);  // Asks ascending (lowest first)

        const maxBidTotal = sortedBids.length > 0 ? sortedBids[sortedBids.length - 1].total : 0;
        const maxAskTotal = sortedAsks.length > 0 ? sortedAsks[sortedAsks.length - 1].total : 0;
        const overallMax = Math.max(maxBidTotal, maxAskTotal, 1);

        sortedBids.forEach(b => b.depthPercent = (b.total / overallMax) * 100);
        sortedAsks.forEach(a => a.depthPercent = (a.total / overallMax) * 100);

        return { bids: sortedBids, asks: sortedAsks };
    }, [snapshot]);

    return (
        <div className="order-book-container">
            <div className="panel-header">Order Book</div>
            <div className="book-headers">
                <div className="book-half">
                    <span>BIDS (VOL)</span>
                    <span>PRICE</span>
                </div>
                <div className="book-half">
                    <span>PRICE</span>
                    <span>ASKS (VOL)</span>
                </div>
            </div>
            
            <div className="book-body mono">
                <div className="book-column bids-column">
                    {bids.map((level) => (
                        <div key={level.price} className="book-row">
                            <div className="depth-bar bg-buy" style={{ width: `${level.depthPercent}%` }} />
                            <span className="qty">{level.quantity.toLocaleString()}</span>
                            <span className="price text-buy">{level.price.toFixed(2)}</span>
                        </div>
                    ))}
                    {bids.length === 0 && <div className="empty-state">No Bids</div>}
                </div>

                <div className="book-column asks-column">
                    {asks.map((level) => (
                        <div key={level.price} className="book-row">
                            <div className="depth-bar bg-sell" style={{ width: `${level.depthPercent}%` }} />
                            <span className="price text-sell">{level.price.toFixed(2)}</span>
                            <span className="qty">{level.quantity.toLocaleString()}</span>
                        </div>
                    ))}
                    {asks.length === 0 && <div className="empty-state">No Asks</div>}
                </div>
            </div>
        </div>
    );
}
