package com.medidropbox.service.impl;

import com.medidropbox.client.Msg91OtpClient;
import com.medidropbox.dto.response.OtpResponse;
import com.medidropbox.service.OtpService;
import org.springframework.stereotype.Service;

/**
 * OTP Service Implementation using MSG91 Widget API v5.
 * - Generate: calls Widget sendOtp, returns reqId.
 * - Verify: calls Widget verifyOtp (get JWT), then verifyAccessToken, returns true if both succeed.
 * - Retry: calls Widget retryOtp, returns new reqId.
 */
@Service
public class OtpServiceImpl implements OtpService {

    private static final long OTP_EXPIRES_SECONDS = 300L; // 5 minutes

    private final Msg91OtpClient msg91OtpClient;

    public OtpServiceImpl(Msg91OtpClient msg91OtpClient) {
        this.msg91OtpClient = msg91OtpClient;
    }

    @Override
    public OtpResponse generateOtp(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("phone is required for OTP generate");
        }
        if (!msg91OtpClient.isConfigured()) {
            throw new IllegalStateException("MSG91 is not configured (msg91.auth-key and msg91.widget-id). Cannot generate OTP.");
        }
        String identifier = normalizePhone(phone);
        Msg91OtpClient.WidgetSendOtpResponse response = msg91OtpClient.sendOtp(identifier);
        if (response == null || !"success".equalsIgnoreCase(response.getType())) {
            throw new RuntimeException("Failed to send OTP: " + (response != null ? response.getMessage() : "no response"));
        }
        String reqId = response.getMessage(); // reqId is in message field
        return OtpResponse.builder()
                .message("OTP sent successfully. Enter the code received on your phone.")
                .expiresInSeconds(OTP_EXPIRES_SECONDS)
                .phone(phone)
                .reqId(reqId)
                .build();
    }

    @Override
    public boolean verifyOtp(String reqId, String otp) {
        if (reqId == null || otp == null || reqId.isBlank() || otp.isBlank()) {
            return false;
        }
        if (!msg91OtpClient.isConfigured()) {
            throw new IllegalStateException("MSG91 is not configured (msg91.auth-key and msg91.widget-id). Cannot verify OTP.");
        }
        // Step 1: Verify OTP - get access token (JWT)
        Msg91OtpClient.WidgetVerifyOtpResponse verifyResponse = msg91OtpClient.verifyOtp(reqId, otp);
        if (verifyResponse == null || !"success".equalsIgnoreCase(verifyResponse.getType())) {
            return false;
        }
        String accessToken = verifyResponse.getMessage(); // JWT is in message field
        
        // Step 2: Verify access token - confirm it's valid
        Msg91OtpClient.WidgetVerifyAccessTokenResponse tokenResponse = msg91OtpClient.verifyAccessToken(accessToken);
        return tokenResponse != null && "success".equalsIgnoreCase(tokenResponse.getType());
    }

    @Override
    public OtpResponse retryOtp(String reqId, String retryChannel) {
        if (reqId == null || reqId.isBlank()) {
            throw new IllegalArgumentException("reqId is required for OTP retry");
        }
        if (!msg91OtpClient.isConfigured()) {
            throw new IllegalStateException("MSG91 is not configured (msg91.auth-key and msg91.widget-id). Cannot retry OTP.");
        }
        Msg91OtpClient.WidgetRetryOtpResponse response = msg91OtpClient.retryOtp(reqId, retryChannel);
        if (response == null || !"success".equalsIgnoreCase(response.getType())) {
            throw new RuntimeException("Failed to retry OTP: " + (response != null ? response.getMessage() : "no response"));
        }
        String newReqId = response.getMessage(); // new reqId is in message field
        return OtpResponse.builder()
                .message("OTP retry sent successfully. Enter the code received on your phone.")
                .expiresInSeconds(OTP_EXPIRES_SECONDS)
                .reqId(newReqId)
                .build();
    }

    /**
     * Normalize phone for MSG91: digits only, with country code (e.g. 919876543210).
     * If 10 digits (Indian local), prepend 91 so MSG91 accepts it (they require international format).
     */
    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 10 && digits.charAt(0) != '0') {
            return "91" + digits;  // India: MSG91 requires country code
        }
        return digits;
    }
}
