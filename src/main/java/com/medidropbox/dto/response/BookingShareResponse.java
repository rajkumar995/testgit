package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingShareResponse {
    private Long id;
    private String shareToken;
    /** Full webpage URL for the friend to open (e.g. https://yourapp.com/shared/{token}) */
    private String shareUrl;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private BookingResponse booking; // Read-only booking details
}
