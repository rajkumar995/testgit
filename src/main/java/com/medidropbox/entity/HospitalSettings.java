package com.medidropbox.entity;

import com.medidropbox.enums.BookingMode;
import com.medidropbox.enums.PendingPatientAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Hospital Settings entity - Booking configuration per hospital
 */
@Entity
@Table(name = "md_hospital_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalSettings extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relationship with Hospital
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false, unique = true)
    private Hospital hospital;

    /**
     * Booking mode: SAME_DAY (only today) or FUTURE_ALLOWED (future days)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_mode", nullable = false)
    @NotNull(message = "Booking mode is required")
    private BookingMode bookingMode = BookingMode.FUTURE_ALLOWED;

    /**
     * Maximum days in advance for booking (0 = today only, 1 = tomorrow, etc.)
     * Only applies when bookingMode = FUTURE_ALLOWED
     */
    @Column(name = "future_booking_days", nullable = false)
    @NotNull(message = "Future booking days is required")
    private Integer futureBookingDays = 0;

    /**
     * Booking start time (e.g., 8:00 AM)
     * Note: Currently not enforced for patients, but stored for future use
     */
    @Column(name = "booking_start_time")
    private LocalTime bookingStartTime;

    /**
     * Booking end time (e.g., 5:00 PM)
     * Note: Currently not enforced for patients, but stored for future use
     */
    @Column(name = "booking_end_time")
    private LocalTime bookingEndTime;

    /**
     * Maximum total bookings per doctor per day (staff + patient)
     */
    @Column(name = "daily_patient_limit", nullable = false)
    @NotNull(message = "Daily patient limit is required")
    private Integer dailyPatientLimit = 30;

    /**
     * Maximum online patient bookings per doctor per day (staff bookings excluded)
     */
    @Column(name = "online_booking_limit", nullable = false)
    @NotNull(message = "Online booking limit is required")
    private Integer onlineBookingLimit = 20;

    /**
     * If false, only staff can create bookings (patients blocked)
     * If true, patients can book online (if other conditions are met)
     */
    @Column(name = "is_online_booking_enabled", nullable = false)
    @NotNull(message = "Online booking enabled flag is required")
    private Boolean isOnlineBookingEnabled = true;

    /**
     * If true, online booking is open (patients can book within limits)
     * If false, online booking is closed (only staff can book)
     */
    @Column(name = "booking_allowed", nullable = false)
    @NotNull(message = "Booking allowed flag is required")
    private Boolean bookingAllowed = true;

    /**
     * Action to take for pending patients (WAITING/CALLED status) at end of day
     * CARRY_OVER: Automatically create new booking for next day with new queue number
     * AUTO_CANCEL: Automatically cancel pending bookings at end of day
     * MANUAL: Staff manually decides what to do (no automatic action)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pending_patient_action", nullable = false)
    @NotNull(message = "Pending patient action is required")
    private PendingPatientAction pendingPatientAction = PendingPatientAction.CARRY_OVER;

    /**
     * Grace period in days for recurring patients (e.g., 7 days)
     * If a patient paid fees, they can revisit within this period without paying again
     */
    @Min(value = 0, message = "Grace period cannot be negative")
    @Column(name = "grace_period_days", nullable = false)
    @NotNull(message = "Grace period days is required")
    private Integer gracePeriodDays = 7;

    /**
     * Maximum number of recurring visits allowed within grace period
     * (e.g., 1 = one free revisit, 2 = two free revisits, etc.)
     */
    @Min(value = 0, message = "Max recurring visits cannot be negative")
    @Column(name = "max_recurring_visits", nullable = false)
    @NotNull(message = "Max recurring visits is required")
    private Integer maxRecurringVisits = 1;

    /**
     * Queue assignment interval for revisit patients
     * After every N new patients, insert 1 revisit patient
     * (e.g., 3 = after every 3 new patients, 1 revisit patient gets queue)
     */
    @Min(value = 1, message = "Revisit queue interval must be at least 1")
    @Column(name = "revisit_queue_interval", nullable = false)
    @NotNull(message = "Revisit queue interval is required")
    private Integer revisitQueueInterval = 3;

    /**
     * Optional URL to QR code image for payment (e.g. UPI QR). Shown on invoice when set.
     */
    @Column(name = "payment_qr_image_url", length = 1024)
    private String paymentQrImageUrl;
}

