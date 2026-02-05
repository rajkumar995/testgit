package com.medidropbox.entity;

import com.medidropbox.enums.QueueStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Queue entity - Live operational state
 * Automatically created when booking is made
 */
@Entity
@Table(name = "md_queues", uniqueConstraints = {
    @UniqueConstraint(name = "UK_queue_doctor_date_number", 
        columnNames = {"doctor_id", "booking_date", "queue_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Queue extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Booking date is required")
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

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
     */
    @NotNull(message = "Global patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_patient_id", nullable = false)
    private GlobalPatient globalPatient;

    @NotNull(message = "Queue number is required")
    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status = QueueStatus.WAITING;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;
}
