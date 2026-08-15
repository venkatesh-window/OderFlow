import { useEffect, useState } from 'react';
import './SystemStatusStrip.css';

interface SystemStatusStripProps {
    isConnected: boolean;
}

export function SystemStatusStrip({ isConnected }: SystemStatusStripProps) {
    const [pulse, setPulse] = useState(false);

    // Simulate subtle processing pulses to show the single-threaded nature
    useEffect(() => {
        if (!isConnected) return;
        const interval = setInterval(() => {
            if (Math.random() > 0.5) {
                setPulse(true);
                setTimeout(() => setPulse(false), 200);
            }
        }, 800);
        return () => clearInterval(interval);
    }, [isConnected]);

    return (
        <div className="status-strip">
            <div className="status-left">
                <span className={`status-indicator ${isConnected ? 'live' : 'offline'}`}>
                    {isConnected ? 'LIVE' : 'OFFLINE'}
                </span>
                <span className="status-text">OrderFlow Engine v1.0</span>
            </div>
            
            <div className="status-right">
                <span className="status-text">
                    Thread Safety: <strong style={{color: 'var(--text-primary)'}}>Strict (Fair Lock)</strong>
                </span>
                <div className={`processing-pulse ${pulse ? 'active' : ''}`} title="Processing orders one by one">
                    Queue Active
                </div>
            </div>
        </div>
    );
}
