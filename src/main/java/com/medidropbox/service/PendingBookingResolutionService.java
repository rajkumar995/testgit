package com.medidropbox.service;

import com.medidropbox.dto.response.ResolvePendingResponse;

import java.time.LocalDate;

/**
 * Resolves pending bookings for past days (before today).
 * Applies hospital setting: AUTO_CANCEL, CARRY_OVER, or MANUAL (skip).
 * Can be called from API (for testing) or from a cron job later.
 */
public interface PendingBookingResolutionService {

    /**
     * Process all pending queues (WAITING/CALLED) with bookingDate &lt; today.
     * For each hospital+date, applies that hospital's pendingPatientAction setting.
     *
     * @return summary (cancelled, carried over, manual skipped counts)
     */
    ResolvePendingResponse resolvePendingBookingsForPastDays();

    /**
     * Process pending queues with bookingDate &lt; upToDateExclusive.
     * Useful for cron or testing a specific cutoff (e.g. upToDateExclusive = today).
     *
     * @param upToDateExclusive process all dates before this date (e.g. LocalDate.now() = process up to yesterday)
     * @return summary
     */
    ResolvePendingResponse resolvePendingBookingsBefore(LocalDate upToDateExclusive);

    /**
     * Process pending bookings for past days, restricted to one hospital.
     * When hospitalId is null, processes all hospitals (same as resolvePendingBookingsForPastDays()).
     *
     * @param hospitalId optional; when non-null, only queues for this hospital are processed (for HOSPITAL_ADMIN)
     * @return summary
     */
    ResolvePendingResponse resolvePendingBookingsForPastDays(Long hospitalId);

    /**
     * Process pending bookings before a date, restricted to one hospital.
     *
     * @param upToDateExclusive process all dates before this date
     * @param hospitalId optional; when non-null, only queues for this hospital are processed
     * @return summary
     */
    ResolvePendingResponse resolvePendingBookingsBefore(LocalDate upToDateExclusive, Long hospitalId);
}
