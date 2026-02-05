package com.medidropbox.enums;

/**
 * Status of a permission request (doctor/hospital requesting access to patient data).
 */
public enum PermissionRequestStatus {
    PENDING,   // Waiting for patient response
    APPROVED,  // Patient granted access
    REJECTED,  // Patient denied
    CANCELLED  // Doctor/hospital cancelled the request
}
