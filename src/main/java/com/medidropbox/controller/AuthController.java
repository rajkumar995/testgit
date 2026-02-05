package com.medidropbox.controller;

import com.medidropbox.dto.request.LoginRequest;
import com.medidropbox.dto.request.RefreshTokenRequest;
import com.medidropbox.dto.request.ResetPasswordRequest;
import com.medidropbox.dto.response.LoginResponse;
import com.medidropbox.dto.response.UserResponse;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user details using JWT token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUserId()));
    }
}
