package com.georeport.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_EXPIRY_SECONDS = 120;
    
    @Autowired
    private EmailService emailService;

    // Concurrent map to store OTP: phoneNumber -> OtpData
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtp(String phoneNumber, String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(phoneNumber, new OtpData(otp, LocalDateTime.now()));
        
        // Send Email
        String subject = "Your GeoReport Verification Code";
        String message = "Your GeoReport verification code is: " + otp + ".\n\nThis code expires in 2 minutes.";
        
        // Asynchronously send email to avoid blocking the API request
        new Thread(() -> {
            try {
                emailService.sendEmail(email, subject, message);
            } catch (Exception e) {
                logger.error("Error sending OTP email: {}", e.getMessage());
            }
        }).start();
        
        return otp;
    }

    public boolean verifyOtp(String phoneNumber, String otp) {
        OtpData data = otpStorage.get(phoneNumber);
        
        if (data == null) {
            return false;
        }

        // Check expiry
        if (data.timestamp.plusSeconds(OTP_EXPIRY_SECONDS).isBefore(LocalDateTime.now())) {
            otpStorage.remove(phoneNumber);
            return false;
        }

        // Check OTP
        if (data.otp.equals(otp)) {
            otpStorage.remove(phoneNumber);
            return true;
        }

        return false;
    }

    private static class OtpData {
        String otp;
        LocalDateTime timestamp;

        OtpData(String otp, LocalDateTime timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }
}
