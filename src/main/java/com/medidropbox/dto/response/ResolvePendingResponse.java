package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response for resolve-pending-bookings API.
 * Summarizes how many bookings were cancelled, carried over, or skipped (MANUAL).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolvePendingResponse {
    /**
     * All dates before this date were processed (e.g. today=15, so up to 14).
     */
    private LocalDate resolvedUpToDateExclusive;

    private int cancelledCount;
    private int carriedOverCount;
    private int manualSkippedCount;
    private int totalQueuesProcessed;

    private String message;
}
