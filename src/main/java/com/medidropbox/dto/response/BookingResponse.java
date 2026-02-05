package com.medidropbox.dto.response;

import com.medidropbox.enums.BookingStatus;
import com.medidropbox.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Role-based booking response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private String patientPhone; // Filtered for non-owners
    private String patientEmail; // Filtered for non-owners
    private Long hospitalId;
    private String hospitalName;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String phoneNumber;
    private String description;
    private java.util.List<String> symptoms;
    private String chiefComplaint;
    private String medicalHistory;
    private String notes;
    private QueueInfo queue;
    private PaymentInfo payment;
    private BookingStatus status;
    
    // Role-based filtering helper
    public static BookingResponse filterByRole(BookingResponse booking, Role role, Long currentUserId, Long currentPatientId) {
        // Patient can only see their own bookings with full details
        if (role == Role.PATIENT && !booking.getPatientId().equals(currentPatientId)) {
            booking.setPatientPhone(null);
            booking.setPatientEmail(null);
        }
        // Staff/Admin see all
        return booking;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueInfo {
        private Long id;
        private Integer queueNumber;
        private String status;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private Long id;
        private String paymentMode;
        private BigDecimal totalBill;
        private BigDecimal totalAmount;
        private BigDecimal discount;
        private BigDecimal taxableAmount;
        private BigDecimal gst;
        private String transactionId;
        /** S3 URL for invoice PDF download */
        private String invoiceUrl;
    }
}
