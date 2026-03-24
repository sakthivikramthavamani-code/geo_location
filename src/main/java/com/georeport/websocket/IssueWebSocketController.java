package com.georeport.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket Controller for real-time messaging.
 * Handles client-to-server WebSocket messages.
 */
@Controller
public class IssueWebSocketController {

    /**
     * Handle subscription to issues topic
     * Client sends: /app/subscribe
     * Response sent to: /topic/issues
     */
    @MessageMapping("/subscribe")
    @SendTo("/topic/issues")
    public Map<String, Object> subscribeToIssues(Map<String, Object> message) {
        return Map.of(
                "eventType", "SUBSCRIPTION_CONFIRMED",
                "message", "Successfully subscribed to issue updates",
                "timestamp", System.currentTimeMillis());
    }

    /**
     * Handle ping messages for connection keepalive
     * Client sends: /app/ping
     * Response sent to: /user/queue/pong
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public Map<String, Object> handlePing(Map<String, Object> message) {
        return Map.of(
                "eventType", "PONG",
                "timestamp", System.currentTimeMillis());
    }
}
