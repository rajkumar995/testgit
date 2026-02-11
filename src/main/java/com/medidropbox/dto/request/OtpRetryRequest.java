package com.medidropbox.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpRetryRequest {
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    /** Request ID from /generate response (required for Widget API). */
    @NotBlank(message = "Request ID is required")
    private String reqId;
    
    /** Optional retry channel (e.g. "sms", "voice"). */
    private String retryChannel;
}
