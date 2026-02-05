package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpResponse {
    private String otp; // For development/testing - remove in production
    private String message;
    private Long expiresInSeconds; // OTP validity period
}
