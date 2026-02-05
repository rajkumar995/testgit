package com.medidropbox.service;

/**
 * OTP Service for phone-based authentication
 */
public interface OtpService {
    /**
     * Generate OTP for phone number (hardcoded: last 4 digits)
     * @param phone Phone number
     * @return OTP (last 4 digits of phone)
     */
    String generateOtp(String phone);
    
    /**
     * Verify OTP
     * @param phone Phone number
     * @param otp OTP to verify
     * @return true if valid
     */
    boolean verifyOtp(String phone, String otp);
}
