# Patient Creation Flow Documentation

## Overview

There are **two ways** to create patients in the system:

1. **Patient Booking Flow** - When a patient creates a booking
2. **Hospital Manual Creation** - When hospital staff manually adds a patient

Both flows handle duplicate prevention and data saving correctly.

---

## 1. Patient Booking Flow

### Endpoint
```
POST {{baseUrl}}/api/v1/bookings
```

### How It Works

1. **Patient creates a booking** with `dataConsent` flag in `BookingRequest`
2. **If `dataConsent = true`**:
   - Full patient data (address, Aadhar, ABHA, emergency contacts) is copied from `GlobalPatient` to `HospitalPatient`
   - All private details are saved
3. **If `dataConsent = false` or not provided**:
   - Only basic data (name, phone, email) is saved
   - Private details (address, Aadhar, ABHA, emergency contacts) are NOT saved

### Duplicate Prevention

- ✅ Checks if patient already exists in the hospital (by phone number)
- ✅ If patient exists and consent is given, updates existing patient with full data
- ✅ If patient exists and no consent, returns existing patient without updating

### BookingRequest Example

```json
{
  "doctorId": 1,
  "hospitalId": 11,
  "bookingDate": "2026-01-15",
  "bookingTime": "10:00:00",
  "phoneNumber": "9876543210",
  "dataConsent": true  // If true, saves full data; if false, only basic data
}
```

### Code Flow

```java
// BookingServiceImpl.java
Boolean dataConsent = request.getDataConsent() != null ? request.getDataConsent() : false;
HospitalPatient hospitalPatient = hospitalPatientService.createOrGetHospitalPatient(
    hospital.getId(), phone, null, null, dataConsent);
```

---

## 2. Hospital Manual Creation Flow

### Endpoint
```
POST {{baseUrl}}/api/v1/hospitals/{{hospitalId}}/patients
```

### How It Works

1. **Hospital staff manually adds a patient** with all fields in `PatientRequest`
2. **All provided fields are saved**:
   - Basic fields: `fullName`, `phone`, `email`
   - Private fields: `aadharId`, `abhaId`
   - Address: Complete address object
   - Emergency contacts: Array of emergency contacts

### Duplicate Prevention

- ✅ Checks if patient already exists in the hospital (by phone number)
- ✅ If patient exists, **UPDATES** existing patient with all new fields provided
- ✅ If patient doesn't exist, **CREATES** new patient with all provided fields
- ✅ **No duplicate patients** are created in the same hospital

### PatientRequest Example

```json
{
  "fullName": "John Doe",
  "phone": "9876543210",
  "email": "john.doe@example.com",
  "aadharId": "123456789012",
  "abhaId": "ABHA123456",
  "address": {
    "addressLine1": "123 Main Street",
    "addressLine2": "Apartment 4B",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "pincode": "400001"
  },
  "emergencyContacts": [
    {
      "personName": "Jane Doe",
      "phone": "9876543211",
      "relationship": "Spouse",
      "isPrimary": true
    }
  ]
}
```

### Code Flow

```java
// HospitalPatientController.java
public ResponseEntity<PatientResponse> createHospitalPatient(
    @PathVariable Long hospitalId,
    @Valid @RequestBody PatientRequest request) {
    return ResponseEntity.ok(
        hospitalPatientService.createHospitalPatientWithFullData(hospitalId, request)
    );
}

// HospitalPatientServiceImpl.java
public PatientResponse createHospitalPatientWithFullData(Long hospitalId, PatientRequest request) {
    // Check if patient already exists
    Optional<HospitalPatient> existing = hospitalPatientRepository
        .findByHospitalAndPhone(hospital, request.getPhone());
    
    if (existing.isPresent()) {
        // Update existing patient with new data
        return updateHospitalPatient(hospitalId, existing.get().getId(), request);
    }
    
    // Create new patient with all provided fields
    // ... saves all fields from request
}
```

---

## Key Differences

| Feature | Booking Flow | Hospital Manual Creation |
|---------|-------------|---------------------------|
| **Data Source** | Copies from `GlobalPatient` | Direct from `PatientRequest` |
| **Consent Required** | Yes (`dataConsent` flag) | No (hospital has permission) |
| **Data Saved** | Based on consent | All fields provided |
| **Duplicate Handling** | Updates if exists + consent | Updates if exists |
| **Use Case** | Patient self-booking | Hospital staff registration |

---

## Duplicate Prevention Logic

Both flows use the same duplicate detection:

```java
Optional<HospitalPatient> existing = hospitalPatientRepository
    .findByHospitalAndPhone(hospital, phone);
```

**Duplicate Detection Criteria:**
- Same `hospitalId`
- Same `phone` number

**If Duplicate Found:**
- ✅ **Booking Flow**: Updates if consent given, otherwise returns existing
- ✅ **Hospital Manual**: Always updates with new data

**If No Duplicate:**
- ✅ Creates new `HospitalPatient` record
- ✅ Links to existing or creates new `GlobalPatient`

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Patient Creation                          │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │   Check: Patient exists in hospital? │
        │   (by phone number)                   │
        └───────────────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
            ▼                               ▼
    ┌───────────────┐              ┌───────────────┐
    │   EXISTS      │              │   NOT EXISTS │
    └───────────────┘              └───────────────┘
            │                               │
            ▼                               ▼
    ┌───────────────┐              ┌───────────────┐
    │   UPDATE      │              │   CREATE      │
    │   Patient     │              │   Patient     │
    └───────────────┘              └───────────────┘
```

---

## Important Notes

1. ✅ **No Duplicates**: Same patient (same phone) cannot be added twice in the same hospital
2. ✅ **Data Preservation**: When updating, existing data is preserved if not provided in request
3. ✅ **Consent Respect**: Booking flow respects patient consent for private data
4. ✅ **Hospital Authority**: Hospital manual creation saves all provided fields (no consent needed)
5. ✅ **Emergency Contacts**: When updating, old emergency contacts are deleted and replaced with new ones

---

## Testing Scenarios

### Scenario 1: Patient Books with Consent
- **Action**: Patient creates booking with `dataConsent: true`
- **Result**: Full data (address, Aadhar, ABHA, emergency contacts) saved
- **Duplicate**: If patient exists, updates with full data

### Scenario 2: Patient Books without Consent
- **Action**: Patient creates booking with `dataConsent: false`
- **Result**: Only basic data (name, phone, email) saved
- **Duplicate**: If patient exists, returns existing without updating

### Scenario 3: Hospital Adds New Patient
- **Action**: Hospital POST `/hospitals/{id}/patients` with full data
- **Result**: All provided fields saved
- **Duplicate**: If patient exists, updates with new data

### Scenario 4: Hospital Adds Existing Patient
- **Action**: Hospital POST `/hospitals/{id}/patients` with same phone
- **Result**: Updates existing patient with new fields
- **Duplicate**: ✅ Prevented - no new record created

---

## API Endpoints Summary

| Endpoint | Method | Flow Type | Consent Required |
|----------|--------|-----------|------------------|
| `/api/v1/bookings` | POST | Booking | Yes (`dataConsent`) |
| `/api/v1/hospitals/{id}/patients` | POST | Hospital Manual | No |

