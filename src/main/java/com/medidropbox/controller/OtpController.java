package com.medidropbox.controller;

import com.medidropbox.dto.request.OtpRequest;
import com.medidropbox.dto.request.OtpVerifyRequest;
import com.medidropbox.dto.response.LoginResponse;
import com.medidropbox.dto.response.OtpResponse;
import com.medidropbox.service.AuthService;
import com.medidropbox.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/otp")
@Tag(name = "OTP Authentication", description = "OTP-based authentication for patients")
public class OtpController {
    
    private final OtpService otpService;
    private final AuthService authService;
    
    public OtpController(OtpService otpService, AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }
    
    @PostMapping("/generate")
    @Operation(summary = "Generate OTP", description = "Generate OTP for phone number. Currently hardcoded: OTP is last 4 digits of phone number.")
    public ResponseEntity<OtpResponse> generateOtp(@Valid @RequestBody OtpRequest request) {
        String otp = otpService.generateOtp(request.getPhone());
        
        OtpResponse response = OtpResponse.builder()
                .otp(otp) // For development/testing - remove in production
                .message("OTP generated successfully. Use last 4 digits of your phone number as OTP.")
                .expiresInSeconds(300L) // 5 minutes
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Verify OTP and Login", description = "Verify OTP and get JWT tokens. If patient doesn't exist, will be created. If UNCLAIMED, will be claimed.")
    public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        LoginResponse loginResponse = authService.loginWithOtp(request.getPhone(), request.getOtp());
        return ResponseEntity.ok(loginResponse);
    }
}
