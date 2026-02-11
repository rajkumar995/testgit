package com.medidropbox.service;

import com.medidropbox.dto.response.OtpResponse;

/**
 * OTP Service for phone-based authentication using MSG91 Widget API v5.
 * - Generate: sends OTP via Widget API, returns reqId.
 * - Verify: verifies OTP, then verifyAccessToken, returns true if verified.
 * - Retry: retries OTP send, returns new reqId.
 */
public interface OtpService {
    /**
     * Send OTP via MSG91 Widget API v5.
     *
     * @param phone Phone number (with or without country code; will be normalized)
     * @return response with reqId (for use in verify/retry)
     */
    OtpResponse generateOtp(String phone);
    
    /**
     * Verify OTP via MSG91 Widget API v5 (verifyOtp + verifyAccessToken).
     * Returns true if both succeed.
     *
     * @param reqId Request ID from generateOtp response
     * @param otp   OTP entered by user
     * @return true if MSG91 verification succeeds (both verifyOtp and verifyAccessToken)
     */
    boolean verifyOtp(String reqId, String otp);
    
    /**
     * Retry OTP send via MSG91 Widget API v5.
     *
     * @param reqId Request ID from generateOtp response
     * @param retryChannel Optional channel (e.g. "sms", "voice")
     * @return response with new reqId
     */
    OtpResponse retryOtp(String reqId, String retryChannel);
}
