package com.medidropbox.dto.request;

import com.medidropbox.enums.LabRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabRequestUpdateRequest {
    
    /**
     * Update status
     */
    private LabRequestStatus status;
    
    /**
     * Assign/reassign lab
     */
    private Long assignedLabId;
    
    /**
     * Sample received by (staff name)
     */
    private String sampleReceivedBy;
    
    /**
     * Rejection reason (if status is SAMPLE_REJECTED)
     */
    private String rejectionReason;
    
    /**
     * Notes
     */
    private String notes;
}
