package com.medidropbox.dto.response;

import com.medidropbox.enums.QueueStatus;
import com.medidropbox.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Role-based queue response
 * PUBLIC: Only current serving queue number
 * PATIENT: Own queue + current serving
 * STAFF/DOCTOR: Full queue details
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueResponse {
    private Long id;
    private LocalDate bookingDate;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private String patientPhone; // Filtered for PUBLIC
    private Integer queueNumber;
    private QueueStatus status;
    private Long bookingId;
    private Integer estimatedWaitTime; // in minutes (position-based, calculated per patient)
    private Integer position; // How many patients ahead (for patient's own queue)

    // For "my queues" list: who is being served now on this doctor/date
    private Integer currentServingQueueNumber;
    private String currentServingPatientStatus; // e.g. "SERVING"

    // Optional context for list view
    private Long hospitalId;
    private String hospitalName;

    // Role-based filtering helper
    public static QueueResponse filterByRole(QueueResponse queue, Role role, Long currentPatientId) {
        if (role == null || role == Role.PUBLIC) {
            // Public: Only show queue number and status, no patient details
            queue.setPatientId(null);
            queue.setPatientName(null);
            queue.setPatientPhone(null);
        } else if (role == Role.PATIENT && !queue.getPatientId().equals(currentPatientId)) {
            // Patient: Hide other patients' details
            queue.setPatientPhone(null);
        }
        // STAFF, DOCTOR, ADMIN see all
        return queue;
    }
}
