# ✅ Corrected Patient Payload for Postman

## Issues Found and Fixed

### 1. ❌ Backend Issue (FIXED)
The backend controller was only saving `phone`, `email`, and `fullName` - it was ignoring `address`, `aadharId`, `abhaId`, and `emergencyContacts`.

**✅ Fixed:** Updated the backend to save all fields from `PatientRequest` during patient creation.

### 2. ❌ Payload Issues (CORRECTED BELOW)

**Your Original Payload:**
```json
{
    "address": {
        "addressLine1":"bihar",
        "street": "123 Main Street",  // ❌ WRONG - "street" doesn't exist
        ...
    },
    "dateOfBirth": "1990-01-15",  // ❌ Not supported in PatientRequest
    "gender": "MALE"  // ❌ Not supported in PatientRequest
}
```

## ✅ CORRECT Payload

### URL
```
POST {{baseUrl}}/api/v1/hospitals/{{hospitalId}}/patients
```

### Headers
```
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

### Corrected Request Body

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
        "pincode": "400001",
        "country": "India",
        "latitude": 19.0760,
        "longitude": 72.8777,
        "locationUrl": "https://maps.google.com/?q=19.0760,72.8777"
    },
    "emergencyContacts": [
        {
            "personName": "Jane Doe",
            "phone": "9876543211",
            "relationship": "Spouse",
            "email": "jane.doe@example.com",
            "isPrimary": true
        }
    ]
}
```

## Key Changes from Your Original Payload

1. ✅ **Removed `"street"`** - Use `"addressLine1"` instead
2. ✅ **Removed `"dateOfBirth"`** - Not supported in PatientRequest
3. ✅ **Removed `"gender"`** - Not supported in PatientRequest
4. ✅ **Fixed address structure** - All address fields should be inside the `address` object
5. ✅ **Added `"addressLine2"`** - Optional field for apartment/suite info
6. ✅ **Added optional fields** - `latitude`, `longitude`, `locationUrl` for better address data

## Field Reference

### Required Fields
- `fullName` - Patient's full name
- `phone` - Phone number (can be with or without country code)

### Optional Fields
- `email` - Valid email address
- `aadharId` - 12-digit Aadhar ID (exactly 12 digits)
- `abhaId` - ABHA Health ID
- `profileImageUrl` - Profile image URL

### Address Object (Optional, but if provided, these are required)
- `addressLine1` - Street address (REQUIRED if address provided)
- `addressLine2` - Apartment, suite, etc. (OPTIONAL)
- `city` - City name (REQUIRED if address provided)
- `state` - State name (REQUIRED if address provided)
- `country` - Country name (REQUIRED if address provided)
- `pincode` - Postal code (REQUIRED if address provided)
- `latitude` - GPS latitude (OPTIONAL)
- `longitude` - GPS longitude (OPTIONAL)
- `locationUrl` - Google Maps URL (OPTIONAL)

### Emergency Contacts Array (Optional)
Each contact object:
- `personName` - Contact person's name (REQUIRED if contact provided)
- `phone` - Contact phone (REQUIRED if contact provided)
- `relationship` - Relationship to patient (OPTIONAL)
- `email` - Contact email (OPTIONAL)
- `isPrimary` - Primary contact flag (OPTIONAL, default: false)

## Expected Response (Success - 200 OK)

```json
{
    "id": 14,
    "fullName": "John Doe",
    "phone": "9876543210",
    "email": "john.doe@example.com",
    "aadharId": "123456789012",
    "abhaId": "ABHA123456",
    "address": {
        "id": 45,
        "addressLine1": "123 Main Street",
        "addressLine2": "Apartment 4B",
        "city": "Mumbai",
        "state": "Maharashtra",
        "country": "India",
        "pincode": "400001",
        "latitude": 19.0760,
        "longitude": 72.8777,
        "locationUrl": "https://maps.google.com/?q=19.0760,72.8777"
    },
    "emergencyContacts": [
        {
            "id": 67,
            "personName": "Jane Doe",
            "phone": "9876543211",
            "relationship": "Spouse",
            "email": "jane.doe@example.com",
            "isPrimary": true,
            "isActive": true
        }
    ],
    "isActive": true,
    "createdAt": "2026-01-02T18:11:51.1361527",
    "updatedAt": "2026-01-02T18:11:51.1361527"
}
```

## Notes

- ✅ Backend now properly saves all fields (address, aadharId, abhaId, emergencyContacts)
- ✅ Use `addressLine1` instead of `street`
- ✅ Remove `dateOfBirth` and `gender` (not supported)
- ✅ All address fields must be inside the `address` object
- ✅ Phone can be with or without country code format

