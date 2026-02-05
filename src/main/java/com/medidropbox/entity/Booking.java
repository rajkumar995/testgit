package com.medidropbox.entity;

import com.medidropbox.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Booking entity - Patient intent to visit
 */
@Entity
@Table(name = "md_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Doctor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /**
     * Hospital-specific patient record (for hospital queries)
     */
    @NotNull(message = "Hospital patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_patient_id", nullable = false)
    private HospitalPatient hospitalPatient;
    
    /**
     * Global patient (for cross-hospital patient queries)
     * Hospital cannot see this, but system uses it for patient to fetch all bookings
     */
    @NotNull(message = "Global patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_patient_id", nullable = false)
    private GlobalPatient globalPatient;

    @NotNull(message = "Hospital is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotNull(message = "Booking date is required")
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @NotNull(message = "Booking time is required")
    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime;

    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * List of symptoms - stored as a collection in a separate table
     */
    @ElementCollection
    @CollectionTable(name = "md_booking_symptoms", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "symptom")
    private java.util.List<String> symptoms;
    
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;
    
    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Queue queue;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;
}
