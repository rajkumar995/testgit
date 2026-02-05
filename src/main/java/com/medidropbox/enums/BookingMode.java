package com.medidropbox.enums;

/**
 * Booking mode enum for hospital settings
 */
public enum BookingMode {
    /**
     * Only allow bookings for the same day (today)
     */
    SAME_DAY,
    
    /**
     * Allow bookings for future days (up to future_booking_days)
     */
    FUTURE_ALLOWED
}

