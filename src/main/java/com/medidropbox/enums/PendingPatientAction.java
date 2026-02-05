package com.medidropbox.enums;

/**
 * Action to take for pending patients at end of day
 */
public enum PendingPatientAction {
    /**
     * Automatically carry over pending patients to next day with new queue number
     */
    CARRY_OVER,
    
    /**
     * Automatically cancel pending patients at end of day
     */
    AUTO_CANCEL,
    
    /**
     * Manual - staff decides what to do with pending patients
     */
    MANUAL
}

