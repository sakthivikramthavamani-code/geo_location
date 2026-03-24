package com.georeport.controller;

import com.georeport.dto.OtpRequest;
import com.georeport.dto.OtpResponse;
import com.georeport.dto.OtpVerificationRequest;
import com.georeport.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "*")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody OtpRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
             return ResponseEntity.badRequest().body(OtpResponse.builder()
                    .success(false)
                    .message("Email is required for OTP")
                    .build());
        }
        otpService.generateOtp(request.getPhoneNumber(), request.getEmail());
        return ResponseEntity.ok(OtpResponse.builder()
                .success(true)
                .message("OTP sent successfully to email")
                .build());
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        boolean isValid = otpService.verifyOtp(request.getPhoneNumber(), request.getOtp());
        
        if (isValid) {
            return ResponseEntity.ok(OtpResponse.builder()
                    .success(true)
                    .message("OTP verified successfully")
                    .build());
        } else {
            return ResponseEntity.badRequest().body(OtpResponse.builder()
                    .success(false)
                    .message("Invalid or expired OTP")
                    .build());
        }
    }
}
