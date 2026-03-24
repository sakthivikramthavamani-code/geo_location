package com.georeport.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OtpServiceTest {

    @InjectMocks
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    private final String phoneNumber = "+1234567890";
    private final String email = "test@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGenerateAndVerifyOtp() {
        String otp = otpService.generateOtp(phoneNumber, email);
        assertNotNull(otp);
        assertEquals(6, otp.length());
        
        assertTrue(otpService.verifyOtp(phoneNumber, otp));
    }

    @Test
    public void testInvalidOtp() {
        otpService.generateOtp(phoneNumber, email);
        assertFalse(otpService.verifyOtp(phoneNumber, "000000"));
    }

    @Test
    public void testExpiredOtp() throws InterruptedException {
        String otp = otpService.generateOtp(phoneNumber, email);
        assertTrue(otpService.verifyOtp(phoneNumber, otp));
    }
}
