import React, { useState, useMemo } from 'react';
import type { OrderBookSnapshot } from '../services/WebSocketClient';
import './OrderEntryPanel.css';

interface OrderEntryPanelProps {
    snapshot: OrderBookSnapshot | null;
}

type OrderType = 'LIMIT' | 'MARKET';
type OrderSide = 'BUY' | 'SELL';

export function OrderEntryPanel({ snapshot }: OrderEntryPanelProps) {
    const [orderType, setOrderType] = useState<OrderType>('LIMIT');
    const [side, setSide] = useState<OrderSide>('BUY');
    const [price, setPrice] = useState<string>('');
    const [quantity, setQuantity] = useState<string>('');
    const [statusMsg, setStatusMsg] = useState<{text: string, type: 'error' | 'success'} | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Generate random trader on mount
    const traderId = useMemo(() => `trader-${Math.floor(Math.random() * 10000)}`, []);
    
    // Quick validation
    const numPrice = parseFloat(price);
    const numQty = parseInt(quantity, 10);
    const isPriceValid = orderType === 'MARKET' || (!isNaN(numPrice) && numPrice > 0);
    const isQtyValid = !isNaN(numQty) && numQty > 0;
    
    const canSubmit = isPriceValid && isQtyValid && !isSubmitting;

    // Estimate fill for limits
    const estimatedFillMsg = useMemo(() => {
        if (!snapshot || !isPriceValid || !isQtyValid || orderType !== 'LIMIT') return null;
        
        let availableQty = 0;
        if (side === 'BUY') {
            // How many asks are <= our buy price?
            for (const [askPriceStr, askQty] of Object.entries(snapshot.asks)) {
                if (parseFloat(askPriceStr) <= numPrice) availableQty += askQty;
            }
        } else {
            // How many bids are >= our sell price?
            for (const [bidPriceStr, bidQty] of Object.entries(snapshot.bids)) {
                if (parseFloat(bidPriceStr) >= numPrice) availableQty += bidQty;
            }
        }
        
        if (availableQty > 0) {
            const fillAmt = Math.min(availableQty, numQty);
            return `This order will likely cross the book and execute immediately (estimated fill: ${fillAmt}).`;
        }
        return null;
    }, [snapshot, isPriceValid, isQtyValid, orderType, side, numPrice, numQty]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!canSubmit) return;
        
        setIsSubmitting(true);
        setStatusMsg(null);
        
        const orderId = `ORD-${Date.now()}`;
        const endpoint = orderType === 'LIMIT' ? 'limit' : 'market';
        const url = new URL(`http://localhost:8080/api/v1/orders/${endpoint}`);
        
        url.searchParams.append('orderId', orderId);
        url.searchParams.append('traderId', traderId);
        url.searchParams.append('traderName', 'GuestTrader');
        url.searchParams.append('side', side);
        url.searchParams.append('quantity', numQty.toString());
        if (orderType === 'LIMIT') {
            url.searchParams.append('price', numPrice.toString());
        }

        try {
            const res = await fetch(url.toString(), { method: 'POST' });
            if (!res.ok) throw new Error('Order submission failed');
            setStatusMsg({ text: 'Order submitted successfully', type: 'success' });
            setQuantity(''); // Reset qty, keep price for convenience
            setTimeout(() => setStatusMsg(null), 3000);
        } catch (err) {
            setStatusMsg({ text: 'Failed to submit order. Check backend connection.', type: 'error' });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="order-entry-container">
            <div className="panel-header">Place Order</div>
            
            <form className="order-form" onSubmit={handleSubmit}>
                <div className="segmented-control">
                    <button type="button" 
                        className={`segment-btn ${side === 'BUY' ? 'active-buy' : ''}`}
                        onClick={() => setSide('BUY')}>BUY</button>
                    <button type="button" 
                        className={`segment-btn ${side === 'SELL' ? 'active-sell' : ''}`}
                        onClick={() => setSide('SELL')}>SELL</button>
                </div>
                
                <div className="segmented-control type-control">
                    <button type="button" 
                        className={`segment-btn ${orderType === 'LIMIT' ? 'active' : ''}`}
                        onClick={() => setOrderType('LIMIT')}>Limit</button>
                    <button type="button" 
                        className={`segment-btn ${orderType === 'MARKET' ? 'active' : ''}`}
                        onClick={() => setOrderType('MARKET')}>Market</button>
                </div>

                {orderType === 'LIMIT' && (
                    <div className="input-group">
                        <label>Price</label>
                        <div className="input-wrapper">
                            <input 
                                type="number" 
                                step="0.01"
                                className="mono"
                                value={price} 
                                onChange={(e) => setPrice(e.target.value)}
                                placeholder="0.00"
                            />
                        </div>
                        {price && !isPriceValid && <div className="validation-msg">Invalid price</div>}
                    </div>
                )}

                <div className="input-group">
                    <label>Quantity</label>
                    <div className="input-wrapper">
                        <input 
                            type="number" 
                            className="mono"
                            value={quantity} 
                            onChange={(e) => setQuantity(e.target.value)}
                            placeholder="0"
                        />
                    </div>
                    {quantity && !isQtyValid && <div className="validation-msg">Invalid quantity</div>}
                </div>
                
                {estimatedFillMsg && (
                    <div className="estimated-fill-msg">
                        {estimatedFillMsg}
                    </div>
                )}

                {statusMsg && (
                    <div className={`status-msg ${statusMsg.type}`}>
                        {statusMsg.text}
                    </div>
                )}

                <button 
                    type="submit" 
                    className={`submit-btn ${side === 'BUY' ? 'btn-buy' : 'btn-sell'}`}
                    disabled={!canSubmit}
                >
                    {side} {orderType} {orderType === 'LIMIT' && isPriceValid ? `@ ${numPrice.toFixed(2)}` : ''}
                </button>
            </form>
        </div>
    );
}
