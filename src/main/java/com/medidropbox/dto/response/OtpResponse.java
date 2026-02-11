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
    private String message;
    private Long expiresInSeconds; // OTP validity period
    /** Phone number (client can use this for /verify). */
    private String phone;
    /** Request ID from MSG91 Widget API (required for /verify and /retry). */
    private String reqId;
}
