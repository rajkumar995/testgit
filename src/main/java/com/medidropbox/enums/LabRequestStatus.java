package com.medidropbox.enums;

/**
 * Lab Request Status
 * Tracks the lifecycle of a lab test request
 */
public enum LabRequestStatus {
    /**
     * Request created by doctor/hospital, waiting for sample
     */
    REQUESTED,
    
    /**
     * Sample received at lab
     */
    SAMPLE_RECEIVED,
    
    /**
     * Test is in progress
     */
    IN_PROGRESS,
    
    /**
     * Test completed, report generated
     */
    COMPLETED,
    
    /**
     * Request cancelled
     */
    CANCELLED,
    
    /**
     * Sample rejected (quality issues, etc.)
     */
    SAMPLE_REJECTED
}
