package com.georeport.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Primary
public class Fast2SmsService implements SmsService {
    private static final Logger logger = LoggerFactory.getLogger(Fast2SmsService.class);

    @Value("${fast2sms.api.key:YOUR_FAST2SMS_API_KEY}")
    private String apiKey;

    @Autowired
    private SmsBroadcastService smsBroadcastService;
    
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendSms(String to, String messageContent) {
        // Always broadcast for Dev Dashboard testing
        smsBroadcastService.broadcastSms(to, messageContent);
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_FAST2SMS_API_KEY") || apiKey.contains("YOUR_")) {
            logger.info("FAST2SMS SIMULATION: To: {}, Content: {}", to, messageContent);
            return;
        }

        try {
            // Clean phone number (expecting exactly 10 digits for India without +91)
            String cleanPhone = to.replaceAll("[^0-9]", "");
            if (cleanPhone.length() > 10) {
                cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            }
            
            // Extract the exact 6 digit OTP from the messageContent to send via OTP route
            Matcher m = Pattern.compile("\\b(\\d{6})\\b").matcher(messageContent);
            String otpCode = m.find() ? m.group(1) : "123456";

            // Using route=otp (best chance to deliver securely via Fast2SMS)
            String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey + 
                         "&route=otp&variables_values=" + otpCode + "&flash=0&numbers=" + cleanPhone;

            // Simple GET Request using Spring RestTemplate
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("Fast2SMS Response: {}", response.getBody());

        } catch (Exception e) {
            logger.error("Failed to send Fast2SMS to {}: {}", to, e.getMessage());
        }
    }
}
