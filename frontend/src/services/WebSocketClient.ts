import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export type Trade = {
    id?: string;
    timestamp?: number;
    buyOrderId: string;
    sellOrderId: string;
    price: number;
    quantity: number;
};

export type OrderBookSnapshot = {
    bids: Record<string, number>; // price -> quantity
    asks: Record<string, number>; // price -> quantity
};

class WebSocketClient {
    private client: Client;

    constructor() {
        this.client = new Client({
            brokerURL: 'ws://localhost:8080/ws-market-data',
            webSocketFactory: () => new SockJS('http://localhost:8080/ws-market-data'),
            debug: function (str) {
                // console.log(str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });
    }

    connect(onTrades: (trades: Trade[]) => void, onOrderBook: (snapshot: OrderBookSnapshot) => void) {
        this.client.onConnect = () => {
            console.log('Connected to WebSocket Gateway');

            this.client.subscribe('/topic/trades', (message) => {
                const trades: Trade[] = JSON.parse(message.body);
                // Inject timestamp and unique ID for frontend tracking
                const enrichedTrades = trades.map(t => ({
                    ...t,
                    id: t.id || `trade-${Date.now()}-${Math.random()}`,
                    timestamp: t.timestamp || Date.now()
                }));
                onTrades(enrichedTrades);
            });

            this.client.subscribe('/topic/orderbook', (message) => {
                const snapshot: OrderBookSnapshot = JSON.parse(message.body);
                onOrderBook(snapshot);
            });
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        };

        this.client.activate();
    }

    disconnect() {
        this.client.deactivate();
    }
}

export const wsClient = new WebSocketClient();
