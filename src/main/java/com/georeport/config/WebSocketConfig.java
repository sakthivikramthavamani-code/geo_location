package com.georeport.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration using STOMP.
 * Enables real-time communication for issue status updates.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory message broker
        // Clients subscribe to /topic/* for broadcasts
        // Clients subscribe to /user/queue/* for personal messages
        registry.enableSimpleBroker("/topic", "/queue");

        // Application destination prefix for @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");

        // User destination prefix for personal messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint that clients connect to
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();

        // Plain WebSocket endpoint without SockJS
        registry.addEndpoint("/ws-plain")
                .setAllowedOrigins("*");
    }
}
