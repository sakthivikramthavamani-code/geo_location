/**
 * GeoReport - WebSocket Client
 * Real-time updates using STOMP over WebSocket
 */

class WebSocketClient {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = [];
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 3000;
    }

    /**
     * Connect to WebSocket server
     */
    connect(onConnected, onError) {
        // Load SockJS and STOMP if not already loaded
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            this.loadDependencies().then(() => {
                this.initializeConnection(onConnected, onError);
            });
        } else {
            this.initializeConnection(onConnected, onError);
        }
    }

    /**
     * Load SockJS and STOMP libraries
     */
    loadDependencies() {
        return new Promise((resolve) => {
            const sockjsScript = document.createElement('script');
            sockjsScript.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js';

            const stompScript = document.createElement('script');
            stompScript.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';

            sockjsScript.onload = () => {
                stompScript.onload = resolve;
                document.head.appendChild(stompScript);
            };

            document.head.appendChild(sockjsScript);
        });
    }

    /**
     * Initialize WebSocket connection
     */
    initializeConnection(onConnected, onError) {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);

        // Disable debug logging in production
        this.stompClient.debug = null;

        this.stompClient.connect({},
            (frame) => {
                console.log('WebSocket connected');
                this.connected = true;
                this.reconnectAttempts = 0;
                if (onConnected) onConnected(frame);
            },
            (error) => {
                console.error('WebSocket error:', error);
                this.connected = false;
                if (onError) onError(error);
                this.attemptReconnect(onConnected, onError);
            }
        );
    }

    /**
     * Attempt to reconnect after connection loss
     */
    attemptReconnect(onConnected, onError) {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);

            setTimeout(() => {
                this.connect(onConnected, onError);
            }, this.reconnectDelay);
        }
    }

    /**
     * Subscribe to issue updates
     */
    subscribeToIssues(callback) {
        if (!this.connected || !this.stompClient) {
            console.warn('WebSocket not connected');
            return null;
        }

        const subscription = this.stompClient.subscribe('/topic/issues', (message) => {
            try {
                const data = JSON.parse(message.body);
                callback(data);
            } catch (e) {
                console.error('Error parsing WebSocket message:', e);
            }
        });

        this.subscriptions.push(subscription);
        return subscription;
    }

    /**
     * Subscribe to personal notifications
     */
    subscribeToNotifications(userId, callback) {
        if (!this.connected || !this.stompClient) {
            console.warn('WebSocket not connected');
            return null;
        }

        const subscription = this.stompClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
            try {
                const data = JSON.parse(message.body);
                callback(data);
            } catch (e) {
                console.error('Error parsing notification:', e);
            }
        });

        this.subscriptions.push(subscription);
        return subscription;
    }

    /**
     * Send ping to keep connection alive
     */
    sendPing() {
        if (this.connected && this.stompClient) {
            this.stompClient.send('/app/ping', {}, JSON.stringify({ timestamp: Date.now() }));
        }
    }

    /**
     * Disconnect from WebSocket
     */
    disconnect() {
        if (this.stompClient) {
            // Unsubscribe from all
            this.subscriptions.forEach(sub => {
                if (sub) sub.unsubscribe();
            });
            this.subscriptions = [];

            this.stompClient.disconnect(() => {
                console.log('WebSocket disconnected');
                this.connected = false;
            });
        }
    }

    /**
     * Check if connected
     */
    isConnected() {
        return this.connected;
    }
}

// Create singleton instance
window.wsClient = new WebSocketClient();
