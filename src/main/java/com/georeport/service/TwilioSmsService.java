package com.georeport.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService implements SmsService {
    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);

    @Autowired
    private SmsBroadcastService smsBroadcastService;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio initialized with Account SID: {}", accountSid);
        } else {
            logger.warn("Twilio is not fully configured. SMS sending will be disabled or fail.");
        }
    }

    @Override
    public void sendSms(String to, String messageContent) {
        // Always broadcast to Dev Dashboard for real-time testing
        smsBroadcastService.broadcastSms(to, messageContent);

        if (!isConfigured()) {
            logger.info("SMS SIMULATION: To: {}, Content: {}", to, messageContent);
            return;
        }

        try {
            // Twilio strictly requires E.164 formatting (+CountryCodeNumber)
            String formattedPhone = to.trim();
            if (!formattedPhone.startsWith("+")) {
                formattedPhone = "+91" + formattedPhone; // Default to India (+91)
            }

            Message message = Message.creator(
                    new PhoneNumber(formattedPhone),
                    new PhoneNumber(fromPhoneNumber),
                    messageContent
            ).create();

            logger.info("SMS sent to {}. SID: {}", formattedPhone, message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", to, e.getMessage());
            throw new RuntimeException("SMS sending failed: " + e.getMessage());
        }
    }

    private boolean isConfigured() {
        return accountSid != null && !accountSid.isEmpty() && !accountSid.contains("YOUR_ACCOUNT_SID") &&
               authToken != null && !authToken.isEmpty() && !authToken.contains("YOUR_AUTH_TOKEN") &&
               fromPhoneNumber != null && !fromPhoneNumber.isEmpty() && !fromPhoneNumber.contains("YOUR_TWILIO_NUMBER");
    }
}
