# 📋 Doctor API Responses - Complete Documentation

## Overview
This document explains **ALL possible responses** that doctors will receive when accessing patient lab reports.

---

## 🔑 Authentication
**All doctor APIs require:**
- JWT Bearer token in `Authorization` header
- Role: `DOCTOR`, `HOSPITAL_ADMIN`, or `HOSPITAL_STAFF`

---

## 📡 API Endpoints

### 1. Get All Patient Reports
**Endpoint:** `GET /api/v1/doctors/lab-reports/patient/{patientId}`

**What Doctor Passes:**
- `patientId` in URL path (regular patient ID, NOT globalPatientId)
- JWT token in Authorization header

---

## ✅ Success Response (200 OK)

### Response When Doctor Has Permission

**Status Code:** `200 OK`

**Response Body:**
```json
[
    {
        "id": 1,
        "patientId": 789,
        "reportType": "XRAY",
        "fileUrl": "https://s3.amazonaws.com/bucket/reports/xray/789/report1.pdf",
        "fileFormat": "PDF",
        "reportDate": "2024-01-15",
        "doctorName": "Dr. Smith",
        "labName": "City Hospital Lab",
        "aiSummary": "Normal chest X-ray, no abnormalities detected",
        "fileName": "chest_xray_2024.pdf",
        "fileSize": 2048576,
        "notes": "Follow-up in 3 months",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
    },
    {
        "id": 2,
        "patientId": 789,
        "reportType": "BLOOD_TEST",
        "fileUrl": "https://s3.amazonaws.com/bucket/reports/blood_test/789/report2.pdf",
        "fileFormat": "PDF",
        "reportDate": "2024-01-10",
        "doctorName": "Dr. Johnson",
        "labName": "LabCorp",
        "aiSummary": "Complete blood count within normal range",
        "fileName": "cbc_report_2024.pdf",
        "fileSize": 1536000,
        "notes": null,
        "createdAt": "2024-01-10T14:20:00",
        "updatedAt": "2024-01-10T14:20:00"
    }
]
```

**Response Fields Explained:**
- `id` - Report ID
- `patientId` - Patient's global patient ID
- `reportType` - Type of report (XRAY, CT_SCAN, MRI, BLOOD_TEST, etc.)
- `fileUrl` - AWS S3 URL to download the report file
- `fileFormat` - File format (PDF, IMAGE, DOC, DOCX, OTHER)
- `reportDate` - Date when report was generated
- `doctorName` - Name of doctor who issued/reviewed the report
- `labName` - Name of lab/hospital where report was generated
- `aiSummary` - AI-generated summary (decrypted)
- `fileName` - Original file name
- `fileSize` - File size in bytes
- `notes` - Additional notes (decrypted, can be null)
- `createdAt` - When report was uploaded
- `updatedAt` - When report was last updated

**Report Types Available:**
- `XRAY`
- `CT_SCAN`
- `MRI`
- `BLOOD_TEST`
- `URINE_TEST`
- `ECG`
- `ULTRASOUND`
- `PATHOLOGY`
- `RADIOLOGY`
- `PRESCRIPTION`
- `DISCHARGE_SUMMARY`
- `OTHER`

**File Formats:**
- `PDF`
- `IMAGE` (JPG, PNG, etc.)
- `DOC`
- `DOCX`
- `OTHER`

---

### Empty List Response (200 OK)

**When:** Patient has no reports OR all reports are inactive

**Status Code:** `200 OK`

**Response Body:**
```json
[]
```

**Meaning:** Doctor has permission, but patient has no reports to show.

---

## ❌ Error Responses

### 1. No Permission (403 Forbidden)

**When:** Doctor does NOT have permission from patient

**Status Code:** `403 Forbidden`

**Response Body:**
```json
{
    "timestamp": "2024-01-20T10:30:00",
    "status": 403,
    "error": "Forbidden",
    "message": "You don't have permission to view this patient's reports. Please ask the patient to grant permission from the app.",
    "path": "/api/v1/doctors/lab-reports/patient/789"
}
```

**What Doctor Should Do:**
1. Ask patient to grant permission via patient app
2. Patient needs to call: `POST /api/v1/patients/lab-reports/permissions`
3. Patient passes: `{"doctorId": 123}` (doctor's ID)
4. Then doctor can retry the request

---

### 2. Unauthorized - No Token (401 Unauthorized)

**When:** JWT token is missing or invalid

**Status Code:** `401 Unauthorized`

**Response Body:**
```json
{
    "timestamp": "2024-01-20T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/v1/doctors/lab-reports/patient/789"
}
```

**What Doctor Should Do:**
- Login again to get a valid JWT token
- Include token in `Authorization: Bearer {token}` header

---

### 3. Doctor/Hospital Not Linked (400 Bad Request)

**When:** Doctor account is not linked to a doctor or hospital record

**Status Code:** `400 Bad Request`

**Response Body:**
```json
{
    "timestamp": "2024-01-20T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Doctor or hospital ID not found. Please ensure you are linked to a doctor or hospital.",
    "path": "/api/v1/doctors/lab-reports/patient/789"
}
```

**What Doctor Should Do:**
- Contact admin to link doctor account to doctor/hospital record
- Ensure doctor profile exists in system

---

### 4. Patient Not Found (404 Not Found)

**When:** Patient ID does not exist in system

**Status Code:** `404 Not Found`

**Response Body:**
```json
{
    "timestamp": "2024-01-20T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Patient not found",
    "path": "/api/v1/doctors/lab-reports/patient/99999"
}
```

**What Doctor Should Do:**
- Verify patient ID is correct
- Check if patient exists in hospital system

---

### 5. Invalid Report Type (400 Bad Request)

**When:** Invalid report type in URL (for filtered endpoint)

**Status Code:** `400 Bad Request`

**Response Body:**
```json
{
    "timestamp": "2024-01-20T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid report type. Valid types: XRAY, CT_SCAN, MRI, BLOOD_TEST, URINE_TEST, ECG, ULTRASOUND, PATHOLOGY, RADIOLOGY, PRESCRIPTION, DISCHARGE_SUMMARY, OTHER",
    "path": "/api/v1/doctors/lab-reports/patient/789/type/INVALID_TYPE"
}
```

---

## 📊 Response Summary Table

| Scenario | Status Code | Response Body | Action Required |
|----------|-------------|---------------|-----------------|
| ✅ Has Permission + Reports Exist | `200 OK` | Array of report objects | None - Use the reports |
| ✅ Has Permission + No Reports | `200 OK` | Empty array `[]` | None - Patient has no reports |
| ❌ No Permission | `403 Forbidden` | Error message | Ask patient to grant permission |
| ❌ No Token/Invalid Token | `401 Unauthorized` | Error message | Login again |
| ❌ Doctor Not Linked | `400 Bad Request` | Error message | Contact admin |
| ❌ Patient Not Found | `404 Not Found` | Error message | Verify patient ID |
| ❌ Invalid Report Type | `400 Bad Request` | Error message | Use valid report type |

---

## 🔄 Complete Flow Example

### Step 1: Doctor Requests Reports (No Permission)
```
GET /api/v1/doctors/lab-reports/patient/789
Authorization: Bearer {token}
```

**Response:**
```json
{
    "status": 403,
    "message": "You don't have permission to view this patient's reports. Please ask the patient to grant permission from the app."
}
```

### Step 2: Patient Grants Permission
```
POST /api/v1/patients/lab-reports/permissions
Authorization: Bearer {patient_token}
Body: {
    "doctorId": 123
}
```

**Response:**
```json
{
    "id": 5,
    "patientId": 789,
    "doctorId": 123,
    "doctorName": "Dr. John Smith",
    "isActive": true,
    "grantedAt": "2024-01-20T10:30:00"
}
```

### Step 3: Doctor Requests Reports Again (Has Permission)
```
GET /api/v1/doctors/lab-reports/patient/789
Authorization: Bearer {token}
```

**Response:**
```json
[
    {
        "id": 1,
        "patientId": 789,
        "reportType": "XRAY",
        "fileUrl": "https://s3.amazonaws.com/...",
        ...
    }
]
```

---

## 📝 Notes

1. **Patient ID vs Global Patient ID:**
   - Doctor passes regular `patientId` (from hospital system)
   - Backend automatically maps to `globalPatientId` internally
   - Doctor doesn't need to know `globalPatientId`

2. **Permission Expiration:**
   - If permission has `expiresAt` date and it's passed, doctor will get 403 error
   - Patient needs to grant permission again

3. **Revoked Permissions:**
   - If patient revokes permission, doctor immediately gets 403 error on next request
   - No notification sent to doctor

4. **Hospital-Level Permissions:**
   - If patient grants permission to hospital, ALL doctors in that hospital can access
   - Doctor doesn't need individual permission if hospital has permission

5. **Report Filtering:**
   - Doctor can filter by report type: `GET /api/v1/doctors/lab-reports/patient/{patientId}/type/XRAY`
   - Same permission check applies
   - Returns only reports of that type

---

## 🎯 Quick Reference

**Doctor Needs:**
- ✅ Valid JWT token
- ✅ Patient ID (regular ID, not global)
- ✅ Permission from patient (granted via patient app)

**Doctor Gets:**
- ✅ All patient reports (if has permission)
- ✅ Reports sorted by date (newest first)
- ✅ Decrypted AI summaries and notes
- ✅ Direct S3 URLs to download files

**Doctor Cannot:**
- ❌ Access reports without permission
- ❌ Request permission directly (must ask patient)
- ❌ See who else has access
- ❌ Modify or delete reports
