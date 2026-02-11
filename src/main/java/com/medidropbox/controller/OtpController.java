package com.medidropbox.controller;

import com.medidropbox.dto.request.OtpRequest;
import com.medidropbox.dto.request.OtpRetryRequest;
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
@Tag(name = "OTP Authentication", description = "OTP-based authentication for patients using MSG91 Widget API v5")
public class OtpController {
    
    private final OtpService otpService;
    private final AuthService authService;
    
    public OtpController(OtpService otpService, AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }
    
    @PostMapping("/generate")
    @Operation(summary = "Generate OTP", description = "Send OTP via MSG91 Widget API. Returns reqId - use this in /verify and /retry.")
    public ResponseEntity<OtpResponse> generateOtp(@Valid @RequestBody OtpRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("phone is required");
        }
        OtpResponse response = otpService.generateOtp(request.getPhone());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Verify OTP and Login", description = "Verify OTP (verifyOtp + verifyAccessToken) and get JWT tokens. Requires reqId from /generate. If patient doesn't exist, will be created. If UNCLAIMED, will be claimed.")
    public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        LoginResponse loginResponse = authService.loginWithOtp(request.getPhone(), request.getReqId(), request.getOtp());
        return ResponseEntity.ok(loginResponse);
    }
    
    @PostMapping("/retry")
    @Operation(summary = "Retry OTP", description = "Retry sending OTP via MSG91 Widget API. Requires reqId from /generate. Returns new reqId.")
    public ResponseEntity<OtpResponse> retryOtp(@Valid @RequestBody OtpRetryRequest request) {
        OtpResponse response = otpService.retryOtp(request.getReqId(), request.getRetryChannel());
        return ResponseEntity.ok(response);
    }
}
