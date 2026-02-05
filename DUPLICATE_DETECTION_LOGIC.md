# Duplicate Patient Detection Logic

## Overview

The system now checks for duplicate patients using **multiple identifiers**:
1. **Phone Number** (primary)
2. **Aadhar ID** (if provided)
3. **ABHA ID** (if provided)

Duplicate detection works differently for:
- **Hospital Patient Creation** (hospital-specific)
- **Global Patient Registration** (system-wide)

---

## 1. Hospital Patient Duplicate Detection

### When Hospital Creates Patient

**Endpoint:** `POST /api/v1/hospitals/{hospitalId}/patients`

**Logic:**
- Checks for duplicate **within the same hospital only**
- Checks by: `phone` OR `aadharId` OR `abhaId`
- If duplicate found: **Updates existing patient** with new data
- If not found: **Creates new patient**

**Code:**
```java
private Optional<HospitalPatient> findDuplicateHospitalPatient(Long hospitalId, PatientRequest request) {
    // Check by phone
    if (request.getPhone() != null) {
        Optional<HospitalPatient> byPhone = hospitalPatientRepository
            .findByHospitalIdAndPhone(hospitalId, request.getPhone());
        if (byPhone.isPresent()) return byPhone;
    }
    
    // Check by Aadhar ID
    if (request.getAadharId() != null) {
        Optional<HospitalPatient> byAadhar = hospitalPatientRepository
            .findByHospitalIdAndAadharId(hospitalId, request.getAadharId());
        if (byAadhar.isPresent()) return byAadhar;
    }
    
    // Check by ABHA ID
    if (request.getAbhaId() != null) {
        Optional<HospitalPatient> byAbha = hospitalPatientRepository
            .findByHospitalIdAndAbhaId(hospitalId, request.getAbhaId());
        if (byAbha.isPresent()) return byAbha;
    }
    
    return Optional.empty();
}
```

**Example Scenarios:**

| Scenario | Phone | Aadhar | ABHA | Result |
|----------|-------|--------|------|--------|
| New patient | 9876543210 | - | - | ✅ Creates new |
| Duplicate phone | 9876543210 | - | - | ✅ Updates existing |
| Duplicate Aadhar | 9999999999 | 123456789012 | - | ✅ Updates existing (same Aadhar) |
| Duplicate ABHA | 8888888888 | - | ABHA123 | ✅ Updates existing (same ABHA) |
| Different hospital | 9876543210 | - | - | ✅ Creates new (different hospital) |

**Key Point:** Each hospital maintains its own patient records. Same patient can exist in multiple hospitals, but not twice in the same hospital.

---

## 2. Global Patient Duplicate Detection

### When Patient Self-Registers

**Endpoint:** `POST /api/v1/patients/register`

**Logic:**
- Checks for duplicate **globally** (across all hospitals)
- Checks by: `phone` OR `aadharId` OR `abhaId`
- If duplicate found and **CLAIMED**: Throws error (already registered)
- If duplicate found and **UNCLAIMED**: Claims the existing patient
- If not found: Creates new **CLAIMED** patient

**Code:**
```java
private Optional<GlobalPatient> findDuplicateGlobalPatient(PatientRequest request) {
    // Check by phone
    if (request.getPhone() != null) {
        Optional<GlobalPatient> byPhone = globalPatientRepository.findByPhone(request.getPhone());
        if (byPhone.isPresent()) return byPhone;
    }
    
    // Check by Aadhar ID
    if (request.getAadharId() != null) {
        Optional<GlobalPatient> byAadhar = globalPatientRepository.findByAadharId(request.getAadharId());
        if (byAadhar.isPresent()) return byAadhar;
    }
    
    // Check by ABHA ID
    if (request.getAbhaId() != null) {
        Optional<GlobalPatient> byAbha = globalPatientRepository.findByAbhaId(request.getAbhaId());
        if (byAbha.isPresent()) return byAbha;
    }
    
    return Optional.empty();
}
```

**Example Scenarios:**

| Scenario | Phone | Aadhar | ABHA | Status | Result |
|----------|-------|--------|------|--------|--------|
| New registration | 9876543210 | - | - | - | ✅ Creates CLAIMED |
| Duplicate phone (CLAIMED) | 9876543210 | - | - | CLAIMED | ❌ Error: Already registered |
| Duplicate phone (UNCLAIMED) | 9876543210 | - | - | UNCLAIMED | ✅ Claims existing |
| Duplicate Aadhar | 9999999999 | 123456789012 | - | CLAIMED | ❌ Error: Already registered |
| Duplicate ABHA | 8888888888 | - | ABHA123 | CLAIMED | ❌ Error: Already registered |

**Key Point:** Global patient registration is system-wide. No duplicate patients allowed globally (by phone, Aadhar, or ABHA).

---

## 3. Booking Flow Duplicate Detection

### When Patient Creates Booking

**Endpoint:** `POST /api/v1/bookings`

**Logic:**
- Uses `createOrGetHospitalPatient()` method
- Checks for duplicate **within the same hospital** by phone only
- If duplicate found: Returns existing patient (may update if consent given)
- If not found: Creates new patient

**Note:** Booking flow only checks by phone (not Aadhar/ABHA) because booking is quick and phone is the primary identifier.

---

## Repository Methods Added

### HospitalPatientRepository

```java
// Find by Aadhar ID within a hospital
Optional<HospitalPatient> findByHospitalIdAndAadharId(Long hospitalId, String aadharId);

// Find by ABHA ID within a hospital
Optional<HospitalPatient> findByHospitalIdAndAbhaId(Long hospitalId, String abhaId);

// Find by any identifier (phone, aadharId, or abhaId) within a hospital
@Query("SELECT hp FROM HospitalPatient hp WHERE hp.hospital.id = :hospitalId AND hp.isActive = true AND " +
       "(hp.phone = :identifier OR hp.aadharId = :identifier OR hp.abhaId = :identifier)")
Optional<HospitalPatient> findByHospitalIdAndAnyIdentifier(@Param("hospitalId") Long hospitalId, 
                                                           @Param("identifier") String identifier);
```

### GlobalPatientRepository

Already has methods:
```java
Optional<GlobalPatient> findByPhone(String phone);
Optional<GlobalPatient> findByAadharId(String aadharId);
Optional<GlobalPatient> findByAbhaId(String abhaId);
```

---

## Summary

| Flow | Scope | Identifiers Checked | Action on Duplicate |
|------|-------|---------------------|-------------------|
| **Hospital Creation** | Hospital-specific | Phone, Aadhar, ABHA | Updates existing |
| **Patient Registration** | Global | Phone, Aadhar, ABHA | Error if CLAIMED, Claims if UNCLAIMED |
| **Booking** | Hospital-specific | Phone only | Returns existing |

---

## Benefits

1. ✅ **No Duplicates**: Prevents same patient being added twice in same hospital
2. ✅ **Flexible Matching**: Matches by phone, Aadhar, or ABHA (whichever is provided)
3. ✅ **Hospital Isolation**: Each hospital has separate patient records
4. ✅ **Global Uniqueness**: Patient can only register once globally
5. ✅ **Data Updates**: Duplicate detection triggers update instead of error

