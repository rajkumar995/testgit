package com.medidropbox.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    // Optional: For patient self-booking, this is ignored (uses authenticated user's globalPatientId)
    // For hospital staff booking, can provide hospitalPatientId or phoneNumber
    private Long patientId; // Hospital patient ID (optional)
    
    @NotNull(message = "Hospital ID is required")
    private Long hospitalId;
    
    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;
    
    @NotNull(message = "Booking time is required")
    private LocalTime bookingTime;
    
    private String phoneNumber;
    
    /**
     * Data consent flag (optional, defaults to false)
     * If true: Full patient data (address, Aadhar, ABHA, emergency contacts) will be copied from GlobalPatient to HospitalPatient
     * If false or null: Only basic data (name, phone, email) will be saved in HospitalPatient
     */
    private Boolean dataConsent = false;
    
    /**
     * Payment details (optional)
     * If provided, payment will be created and linked to the booking
     * Payment can also be created separately later if not provided during booking
     */
    @Valid
    private PaymentRequest payment;
    
    /**
     * Booking description/reason (optional)
     * Patient can provide a description or reason for the visit
     */
    private String description;
    
    /**
     * List of symptoms (optional)
     * Patient can provide a list of symptoms they are experiencing
     */
    private java.util.List<String> symptoms;
    
    /**
     * Chief complaint (optional, for future use)
     * Medical term for the main reason for the visit
     */
    private String chiefComplaint;
    
    /**
     * Medical history (optional, for future use)
     * Brief medical history relevant to this visit
     */
    private String medicalHistory;
    
    /**
     * Additional notes (optional, for future use)
     * Any additional notes or information the patient wants to share
     */
    private String notes;

    // ---------- Book for friend (another person) ----------
    /**
     * When set (with bookForPhone), booking is for another person. Only Hospital Patient is created/updated; no Global Patient for friend.
     * Owner of booking remains the logged-in user; friend accesses via Share.
     */
    private String bookForName;
    private String bookForPhone;
    private java.time.LocalDate bookForDateOfBirth;
    private String bookForEmail;
    /** Description/reason for friend's visit (stored in Hospital Patient notes). */
    private String bookForDescription;
    /** Compliance: must be true when booking for another person. "I have consent to book on behalf of this person." */
    private Boolean bookOnBehalfConsent;
}
