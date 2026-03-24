package com.georeport.service;

/**
 * Interface for SMS service operations.
 */
public interface SmsService {
    /**
     * Send an SMS message
     * @param to Phone number to send the message to
     * @param message The message content
     */
    void sendSms(String to, String message);
}
