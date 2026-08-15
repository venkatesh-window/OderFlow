import { useMemo } from 'react';
import type { OrderBookSnapshot } from '../services/WebSocketClient';

interface DepthChartProps {
    snapshot: OrderBookSnapshot | null;
}

export function DepthChart({ snapshot }: DepthChartProps) {
    const { bidPoints, askPoints, minPrice, maxPrice } = useMemo(() => {
        if (!snapshot) return { bidPoints: '', askPoints: '', minPrice: 0, maxPrice: 0, maxDepth: 0 };

        // Sort bids descending
        const sortedBids = Object.keys(snapshot.bids)
            .map(Number)
            .sort((a, b) => b - a);
            
        // Sort asks ascending
        const sortedAsks = Object.keys(snapshot.asks)
            .map(Number)
            .sort((a, b) => a - b);

        let currentDepth = 0;
        const bidData = sortedBids.map(p => {
            currentDepth += snapshot.bids[p];
            return { price: p, depth: currentDepth };
        });

        currentDepth = 0;
        const askData = sortedAsks.map(p => {
            currentDepth += snapshot.asks[p];
            return { price: p, depth: currentDepth };
        });

        if (bidData.length === 0 && askData.length === 0) {
            return { bidPoints: '', askPoints: '', minPrice: 0, maxPrice: 0 };
        }

        const midPrice = (
            (bidData.length > 0 ? bidData[0].price : 0) + 
            (askData.length > 0 ? askData[0].price : 0)
        ) / 2 || (bidData.length ? bidData[0].price : askData[0].price);

        // Find bounds
        const maxDepthVal = Math.max(
            bidData.length ? bidData[bidData.length - 1].depth : 0,
            askData.length ? askData[askData.length - 1].depth : 0,
            1
        );
        
        // Let's cap the price range to +/- 10% of mid price for a good zoom, 
        // or just use min/max of the top 20 levels.
        const minP = bidData.length ? bidData[Math.min(bidData.length-1, 20)].price : midPrice * 0.9;
        const maxP = askData.length ? askData[Math.min(askData.length-1, 20)].price : midPrice * 1.1;

        // Generate SVG polygon points (1000x200 coordinate space)
        const W = 1000;
        const H = 200;
        const priceRange = maxP - minP;

        const getX = (p: number) => ((p - minP) / priceRange) * W;
        const getY = (d: number) => H - ((d / maxDepthVal) * H);

        // Bid polygon (green)
        let bPts = `${getX(minP)},${H} `;
        for (let i = bidData.length - 1; i >= 0; i--) {
            if (bidData[i].price < minP) continue;
            // Step interpolation
            if (i < bidData.length - 1) {
                bPts += `${getX(bidData[i].price)},${getY(bidData[i+1].depth)} `;
            } else {
                bPts += `${getX(bidData[i].price)},${H} `;
            }
            bPts += `${getX(bidData[i].price)},${getY(bidData[i].depth)} `;
        }
        bPts += `${getX(midPrice)},${getY(bidData.length ? bidData[0].depth : 0)} `;
        bPts += `${getX(midPrice)},${H}`;

        // Ask polygon (red)
        let aPts = `${getX(midPrice)},${H} `;
        aPts += `${getX(midPrice)},${getY(askData.length ? askData[0].depth : 0)} `;
        for (let i = 0; i < askData.length; i++) {
            if (askData[i].price > maxP) continue;
            aPts += `${getX(askData[i].price)},${getY(askData[i].depth)} `;
            if (i < askData.length - 1) {
                aPts += `${getX(askData[i+1].price)},${getY(askData[i].depth)} `;
            } else {
                aPts += `${getX(askData[i].price)},${H} `;
            }
        }
        aPts += `${getX(maxP)},${H}`;

        return {
            bidPoints: bPts,
            askPoints: aPts,
            minPrice: minP,
            maxPrice: maxP
        };
    }, [snapshot]);

    if (!snapshot || (bidPoints === '' && askPoints === '')) {
        return <div style={{ height: 150, borderTop: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: 12 }}>Awaiting Market Data</div>;
    }

    return (
        <div style={{ height: 150, borderTop: '1px solid var(--border-color)', position: 'relative' }}>
            <div style={{ position: 'absolute', top: 4, left: 8, fontSize: 10, color: 'var(--text-muted)' }}>DEPTH</div>
            <svg width="100%" height="100%" viewBox="0 0 1000 200" preserveAspectRatio="none">
                <polygon points={bidPoints} fill="var(--buy-bg-intense)" stroke="var(--buy-color)" strokeWidth="2" strokeLinejoin="round" />
                <polygon points={askPoints} fill="var(--sell-bg-intense)" stroke="var(--sell-color)" strokeWidth="2" strokeLinejoin="round" />
            </svg>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0 8px', fontSize: 10, color: 'var(--text-secondary)' }}>
                <span>{minPrice.toFixed(2)}</span>
                <span>{maxPrice.toFixed(2)}</span>
            </div>
        </div>
    );
}
