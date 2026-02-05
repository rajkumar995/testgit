package com.medidropbox.enums;

/**
 * Fixed roles for MediDropBox system
 */
public enum Role {
    SUPER_ADMIN,
    ADMIN,
    PRODUCT_ADMIN,
    HOSPITAL_ADMIN,  // Hospital administrator - full access to their hospital
    HOSPITAL_STAFF,  // Regular hospital staff
    DOCTOR,
    PATIENT,
    PUBLIC
}
