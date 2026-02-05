package com.medidropbox.service;

import com.medidropbox.dto.request.LoginRequest;
import com.medidropbox.dto.request.RefreshTokenRequest;
import com.medidropbox.dto.request.ResetPasswordRequest;
import com.medidropbox.dto.response.LoginResponse;
import com.medidropbox.dto.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse loginWithOtp(String phone, String otp);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void resetPassword(ResetPasswordRequest request);
    UserResponse getCurrentUser(Long userId);
}
