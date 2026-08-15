import { useEffect, useState } from 'react';
import type { Trade } from '../services/WebSocketClient';
import './MatchReplay.css';

interface MatchReplayProps {
    trade: Trade;
    onClose: () => void;
}

export function MatchReplay({ trade, onClose }: MatchReplayProps) {
    const [step, setStep] = useState(0);

    // Simulate the animation steps
    useEffect(() => {
        const timer1 = setTimeout(() => setStep(1), 800); // Show incoming order
        const timer2 = setTimeout(() => setStep(2), 2000); // Highlight resting order in book
        const timer3 = setTimeout(() => setStep(3), 3500); // Show match & fill
        return () => {
            clearTimeout(timer1);
            clearTimeout(timer2);
            clearTimeout(timer3);
        };
    }, [trade]);

    // Mocking which one is the aggressor for educational purposes
    // since we don't have the exact aggressor side from backend
    const isBuyAggressor = Math.random() > 0.5;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Trade Match Replay</h2>
                    <button className="close-btn" onClick={onClose}>&times;</button>
                </div>
                
                <div className="replay-body">
                    <p className="explanation text-muted">
                        This replay demonstrates <strong>Price-Time Priority (FIFO)</strong>.
                    </p>

                    <div className="animation-stage">
                        {/* Step 0/1: Incoming Order */}
                        <div className={`order-box incoming ${step >= 1 ? 'slide-in' : ''} ${isBuyAggressor ? 'buy' : 'sell'}`}>
                            <div className="badge">Incoming Aggressor</div>
                            <div className="mono">ID: {isBuyAggressor ? trade.buyOrderId.substring(0, 8) : trade.sellOrderId.substring(0, 8)}</div>
                            <div className="mono">Side: {isBuyAggressor ? 'BUY' : 'SELL'}</div>
                            <div className="mono">Qty: {trade.quantity}</div>
                        </div>

                        {/* Middle: Match Icon */}
                        <div className={`match-icon ${step >= 3 ? 'visible' : ''}`}>
                            ⚡ MATCH ⚡<br/>
                            <span className="mono">@ {trade.price.toFixed(2)}</span>
                        </div>

                        {/* Step 0/2: Resting Order */}
                        <div className={`order-box resting ${step >= 2 ? 'highlight' : ''} ${!isBuyAggressor ? 'buy' : 'sell'}`}>
                            <div className="badge">Resting Order (Oldest Timestamp)</div>
                            <div className="mono">ID: {!isBuyAggressor ? trade.buyOrderId.substring(0, 8) : trade.sellOrderId.substring(0, 8)}</div>
                            <div className="mono">Side: {!isBuyAggressor ? 'BUY' : 'SELL'}</div>
                            <div className="mono">Price: {trade.price.toFixed(2)}</div>
                        </div>
                    </div>

                    <div className="step-explanations">
                        <div className={`step-text ${step >= 1 ? 'active' : ''}`}>
                            <strong>1. Order Arrival:</strong> A new {isBuyAggressor ? 'Buy' : 'Sell'} order enters the Matching Engine.
                        </div>
                        <div className={`step-text ${step >= 2 ? 'active' : ''}`}>
                            <strong>2. Book Scan (Price-Time):</strong> The engine scans the opposite book. It finds the best price ({trade.price.toFixed(2)}). If multiple orders share this price, it picks the oldest one.
                        </div>
                        <div className={`step-text ${step >= 3 ? 'active' : ''}`}>
                            <strong>3. Execution:</strong> The orders cross. The trade is executed at the resting order's price, providing price improvement to the aggressor.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
