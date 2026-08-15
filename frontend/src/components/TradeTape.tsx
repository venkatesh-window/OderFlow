import React from 'react';
import type { Trade } from '../services/WebSocketClient';
import './TradeTape.css';

interface TradeTapeProps {
    trades: Trade[];
    onTradeClick: (trade: Trade) => void;
}

export function TradeTape({ trades, onTradeClick }: TradeTapeProps) {
    const formatTime = (ts?: number) => {
        if (!ts) return '';
        const d = new Date(ts);
        return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`;
    };

    return (
        <div className="trade-tape-container">
            <div className="panel-header">Recent Trades</div>
            <div className="tape-headers">
                <span className="col-price">PRICE</span>
                <span className="col-qty">QTY</span>
                <span className="col-time">TIME</span>
            </div>
            
            <div className="tape-body mono">
                {trades.map((trade, i) => {
                    // For UI purposes, we'll alternate color slightly or determine aggressor side if we could
                    // Since we don't know the exact aggressor from the basic payload, 
                    // we can use a neutral color or random for mock purposes, but let's just use white for tape text
                    // or alternate based on if price moved up/down compared to previous trade.
                    const prevTrade = trades[i + 1];
                    let priceClass = 'text-primary';
                    if (prevTrade) {
                        if (trade.price > prevTrade.price) priceClass = 'text-buy';
                        else if (trade.price < prevTrade.price) priceClass = 'text-sell';
                    }

                    return (
                        <div 
                            key={trade.id || i} 
                            className="tape-row"
                            onClick={() => onTradeClick(trade)}
                        >
                            <span className={`col-price ${priceClass}`}>{trade.price.toFixed(2)}</span>
                            <span className="col-qty">{trade.quantity.toLocaleString()}</span>
                            <span className="col-time text-muted">{formatTime(trade.timestamp)}</span>
                        </div>
                    );
                })}
                {trades.length === 0 && <div className="empty-state">Waiting for trades...</div>}
            </div>
        </div>
    );
}
