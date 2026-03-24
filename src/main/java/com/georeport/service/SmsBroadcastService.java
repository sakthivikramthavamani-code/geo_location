package com.georeport.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to broadcast SMS messages to a WebSocket topic for developer testing.
 */
@Service
public class SmsBroadcastService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Broadcast an SMS message to the /topic/dev-sms WebSocket topic.
     * @param to The recipient phone number
     * @param content The SMS message content
     */
    public void broadcastSms(String to, String content) {
        Map<String, String> payload = new HashMap<>();
        payload.put("to", to);
        payload.put("content", content);
        payload.put("timestamp", LocalDateTime.now().format(formatter));
        
        messagingTemplate.convertAndSend("/topic/dev-sms", payload);
    }
}
