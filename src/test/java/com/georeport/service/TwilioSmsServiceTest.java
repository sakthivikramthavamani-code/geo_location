package com.georeport.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TwilioSmsServiceTest {

    private TwilioSmsService twilioSmsService;

    @BeforeEach
    public void setUp() {
        twilioSmsService = new TwilioSmsService();
    }

    @Test
    public void testIsConfiguredFalseWhenEmpty() {
        ReflectionTestUtils.setField(twilioSmsService, "accountSid", "");
        ReflectionTestUtils.setField(twilioSmsService, "authToken", "");
        ReflectionTestUtils.setField(twilioSmsService, "fromPhoneNumber", "");

        // Since isConfigured is private, we can't call it directly without reflection
        // but we can test that sendSms doesn't throw a null pointer exception if it's not configured
        assertDoesNotThrow(() -> twilioSmsService.sendSms("+1234567890", "test"));
    }

    @Test
    public void testIsConfiguredFalseWhenPlaceholders() {
        ReflectionTestUtils.setField(twilioSmsService, "accountSid", "YOUR_ACCOUNT_SID_HERE");
        ReflectionTestUtils.setField(twilioSmsService, "authToken", "YOUR_AUTH_TOKEN_HERE");
        ReflectionTestUtils.setField(twilioSmsService, "fromPhoneNumber", "YOUR_TWILIO_NUMBER_HERE");

        assertDoesNotThrow(() -> twilioSmsService.sendSms("+1234567890", "test"));
    }
}
