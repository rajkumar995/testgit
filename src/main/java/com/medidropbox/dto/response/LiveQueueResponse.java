package com.medidropbox.dto.response;

import com.medidropbox.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Live queue response for a doctor on a specific date
 * Role-based filtering applied
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveQueueResponse {
    private Long doctorId;
    private String doctorName;
    private Long hospitalId;
    private String hospitalName;
    private java.time.LocalDate bookingDate;
    private Integer currentServingQueueNumber;
    private Integer totalWaiting;
    private Integer estimatedWaitTime; // in minutes (general estimate)
    private CurrentServingPatientInfo currentServingPatient; // Current serving patient details
    private MyQueueInfo myQueue; // Patient's own queue info (if authenticated as PATIENT)
    private List<QueueResponse> queues; // Filtered based on role
    private Integer averageConsultationTime; // Average consultation time per patient in minutes
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentServingPatientInfo {
        private Integer queueNumber;
        private String status; // SERVING
        private String patientName; // Optional, filtered based on role
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyQueueInfo {
        private Integer queueNumber;
        private String status; // WAITING, CALLED, SERVING, etc.
        private Integer position; // How many patients ahead (0 if currently serving)
        private Integer estimatedWaitTime; // Position-based wait time in minutes
    }
    
    // Role-based filtering helper
    public static LiveQueueResponse filterByRole(LiveQueueResponse liveQueue, Role role, Long currentPatientId) {
        if (liveQueue.getQueues() != null) {
            liveQueue.getQueues().forEach(queue -> 
                QueueResponse.filterByRole(queue, role, currentPatientId)
            );
        }
        return liveQueue;
    }
}
