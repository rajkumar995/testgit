package com.medidropbox.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingShareRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    /** Optional. If null or in the past, server sets expiry to now + 7 days. */
    private LocalDateTime expiresAt;
}
