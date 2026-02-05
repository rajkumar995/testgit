package com.medidropbox.service;

import com.medidropbox.dto.request.BookingRequest;
import com.medidropbox.dto.response.BookingResponse;
import com.medidropbox.enums.Role;

import java.util.List;

public interface BookingService {
    /**
     * Create booking
     * @param request Booking request
     * @param globalPatientId Global patient ID (for patient self-booking) or null (for hospital staff booking)
     * @param createdByUserId User ID who created (for audit when booking for another person); nullable
     */
    BookingResponse createBooking(BookingRequest request, Long globalPatientId, Long createdByUserId);
    
    /**
     * Get all bookings for a patient across ALL hospitals (cross-hospital query)
     * @param globalPatientId Global patient ID
     */
    List<BookingResponse> getMyBookings(Long globalPatientId, Role role);
    
    /**
     * Get bookings for a hospital (hospital-scoped query)
     */
    List<BookingResponse> getBookingsByHospital(Long hospitalId, Role role);
    
    /**
     * Get bookings for a doctor
     */
    List<BookingResponse> getBookingsByDoctor(Long doctorId, Role role);
    
    /**
     * Get booking by ID
     */
    BookingResponse getBookingById(Long id, Role role, Long currentUserId, Long currentPatientId);
}
