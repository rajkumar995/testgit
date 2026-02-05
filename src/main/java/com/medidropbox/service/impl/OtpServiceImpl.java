package com.medidropbox.service.impl;

import com.medidropbox.service.OtpService;
import org.springframework.stereotype.Service;

/**
 * OTP Service Implementation
 * Currently hardcoded: OTP is last 4 digits of phone number
 */
@Service
public class OtpServiceImpl implements OtpService {
    
    @Override
    public String generateOtp(String phone) {
        // Hardcoded: OTP is last 4 digits of phone number
        if (phone == null || phone.length() < 4) {
            throw new RuntimeException("Invalid phone number");
        }
        
        // Extract last 4 digits (remove any non-digit characters first)
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 4) {
            throw new RuntimeException("Phone number must have at least 4 digits");
        }
        
        return digitsOnly.substring(digitsOnly.length() - 4);
    }
    
    @Override
    public boolean verifyOtp(String phone, String otp) {
        if (phone == null || otp == null) {
            return false;
        }
        
        String expectedOtp = generateOtp(phone);
        return expectedOtp.equals(otp);
    }
}
