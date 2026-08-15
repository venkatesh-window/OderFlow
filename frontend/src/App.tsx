import React, { useEffect, useState } from 'react';
import { wsClient } from './services/WebSocketClient';
import type { Trade, OrderBookSnapshot } from './services/WebSocketClient';
import './index.css';

// Placeholder components - we will build these out individually
import { OrderBook } from './components/OrderBook';
import { OrderEntryPanel } from './components/OrderEntryPanel';
import { TradeTape } from './components/TradeTape';
import { SystemStatusStrip } from './components/SystemStatusStrip';
import { MatchReplay } from './components/MatchReplay';
import { DepthChart } from './components/DepthChart';

function App() {
    const [trades, setTrades] = useState<Trade[]>([]);
    const [snapshot, setSnapshot] = useState<OrderBookSnapshot | null>(null);
    const [isConnected, setIsConnected] = useState(false);
    
    // For Match Replay
    const [selectedTrade, setSelectedTrade] = useState<Trade | null>(null);

    useEffect(() => {
        // Connect to WebSocket and pass callbacks
        wsClient.connect(
            (newTrades) => {
                setTrades(prev => {
                    const combined = [...newTrades, ...prev];
                    return combined.slice(0, 100); // Keep last 100 trades
                });
            },
            (newSnapshot) => {
                setSnapshot(newSnapshot);
            }
        );

        // Simple hack to track connection status (wsClient could be updated to emit this)
        setTimeout(() => setIsConnected(true), 1000); 

        return () => wsClient.disconnect();
    }, []);

    return (
        <div className="app-container">
            <header className="top-bar">
                <div className="brand">ORDERFLOW TERMINAL</div>
            </header>
            
            <main className="main-workspace">
                {/* Left Panel: Order Entry */}
                <div style={{ flex: '0 0 320px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <div className="panel" style={{ flex: 1 }}>
                        <OrderEntryPanel snapshot={snapshot} />
                    </div>
                </div>
                
                {/* Center Panel: Order Book & Depth Chart */}
                <div className="panel" style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                    <OrderBook snapshot={snapshot} />
                    <DepthChart snapshot={snapshot} />
                </div>
                
                {/* Right Panel: Trade Tape */}
                <div className="panel" style={{ flex: '0 0 350px' }}>
                    <TradeTape trades={trades} onTradeClick={setSelectedTrade} />
                </div>
            </main>

            <SystemStatusStrip isConnected={isConnected} />
            
            {selectedTrade && (
                <MatchReplay trade={selectedTrade} onClose={() => setSelectedTrade(null)} />
            )}
        </div>
    );
}

export default App;
