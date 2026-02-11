package com.medidropbox.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabRequestCreateRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Lab test ID is required")
    private Long labTestId;
    
    /**
     * Lab/Hospital ID to assign the test to (optional - can be assigned later)
     */
    private Long assignedLabId;
    
    /**
     * Request notes/instructions
     */
    private String requestNotes;
    
    /**
     * Priority: NORMAL, URGENT, STAT
     */
    private String priority = "NORMAL";
}
