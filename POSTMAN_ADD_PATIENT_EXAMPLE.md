# Postman Request: Add Patient to Hospital

## Endpoint
**Method:** `POST`  
**URL:** `{{baseUrl}}/api/v1/hospitals/{{hospitalId}}/patients`

**Example URL:**
```
http://localhost:8080/api/v1/hospitals/11/patients
```

## Headers
```
Content-Type: application/json
Authorization: Bearer {{accessToken}}
```

## Request Body (JSON)

### Complete Example with All Fields:

```json
{
  "fullName": "Rajesh Kumar",
  "phone": "9876543210",
  "email": "rajesh.kumar@example.com",
  "aadharId": "123456789012",
  "abhaId": "ABHA123456789",
  "profileImageUrl": "https://example.com/profile.jpg",
  "address": {
    "addressLine1": "123 Main Street",
    "addressLine2": "Apartment 4B, Building A",
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
      "personName": "Priya Kumar",
      "phone": "9876543211",
      "relationship": "Spouse",
      "email": "priya.kumar@example.com",
      "isPrimary": true
    },
    {
      "personName": "Ramesh Kumar",
      "phone": "9876543212",
      "relationship": "Father",
      "email": "ramesh.kumar@example.com",
      "isPrimary": false
    }
  ]
}
```

### ⚠️ Common Payload Mistakes:

**❌ WRONG:**
```json
{
  "address": {
    "street": "123 Main Street",  // ❌ Wrong key - should be "addressLine1"
    "city": "Mumbai"
  },
  "dateOfBirth": "1990-01-15",  // ❌ Not supported in PatientRequest
  "gender": "MALE"  // ❌ Not supported in PatientRequest
}
```

**✅ CORRECT:**
```json
{
  "address": {
    "addressLine1": "123 Main Street",  // ✅ Correct key
    "addressLine2": "Apartment 4B",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "pincode": "400001"
  }
}
```

### Minimal Example (Required Fields Only):

```json
{
  "fullName": "Rajesh Kumar",
  "phone": "+91-9876543210"
}
```

### Example with Address Only:

```json
{
  "fullName": "Rajesh Kumar",
  "phone": "+91-9876543210",
  "email": "rajesh.kumar@example.com",
  "address": {
    "addressLine1": "123 Main Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "pincode": "400001"
  }
}
```

### Example with Emergency Contact Only:

```json
{
  "fullName": "Rajesh Kumar",
  "phone": "+91-9876543210",
  "emergencyContacts": [
    {
      "personName": "Priya Kumar",
      "phone": "+91-9876543211",
      "relationship": "Spouse",
      "isPrimary": true
    }
  ]
}
```

## Field Descriptions

### Required Fields:
- **fullName** (String): Patient's full name
- **phone** (String): Patient's phone number (format: +91-XXXXXXXXXX)

### Optional Fields:
- **email** (String): Valid email address
- **aadharId** (String): 12-digit Aadhar ID (must be exactly 12 digits)
- **abhaId** (String): ABHA Health ID
- **profileImageUrl** (String): URL to patient's profile image

### Address Object (Optional):
- **addressLine1** (String, Required if address provided): Street address
- **addressLine2** (String, Optional): Apartment, suite, building, etc.
- **city** (String, Required if address provided): City name
- **state** (String, Required if address provided): State name
- **country** (String, Required if address provided): Country name
- **pincode** (String, Required if address provided): Postal/ZIP code
- **latitude** (Double, Optional): GPS latitude
- **longitude** (Double, Optional): GPS longitude
- **locationUrl** (String, Optional): Google Maps or location URL

### Emergency Contacts Array (Optional):
Each contact object contains:
- **personName** (String, Required): Contact person's name
- **phone** (String, Required): Contact person's phone number
- **relationship** (String, Optional): Relationship to patient (e.g., "Spouse", "Father", "Mother", "Friend")
- **email** (String, Optional): Contact person's email
- **isPrimary** (Boolean, Optional): Whether this is the primary emergency contact (default: false)

## Validation Rules

1. **fullName**: Required, cannot be blank
2. **phone**: Required, cannot be blank
3. **email**: If provided, must be a valid email format
4. **aadharId**: If provided, must be exactly 12 digits (numeric only)
5. **address**: If provided, addressLine1, city, state, country, and pincode are required
6. **emergencyContacts**: If provided, each contact must have personName and phone

## Example Response (Success - 200 OK)

```json
{
  "id": 123,
  "fullName": "Rajesh Kumar",
  "phone": "+91-9876543210",
  "email": "rajesh.kumar@example.com",
  "aadharId": "123456789012",
  "abhaId": "ABHA123456789",
  "address": {
    "id": 456,
    "addressLine1": "123 Main Street",
    "addressLine2": "Apartment 4B, Building A",
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
      "id": 789,
      "personName": "Priya Kumar",
      "phone": "+91-9876543211",
      "relationship": "Spouse",
      "email": "priya.kumar@example.com",
      "isPrimary": true
    },
    {
      "id": 790,
      "personName": "Ramesh Kumar",
      "phone": "+91-9876543212",
      "relationship": "Father",
      "email": "ramesh.kumar@example.com",
      "isPrimary": false
    }
  ],
  "hospitalId": 11,
  "globalPatientId": 234,
  "isActive": true,
  "createdAt": "2025-01-02T10:30:00",
  "updatedAt": "2025-01-02T10:30:00"
}
```

## Notes

- Replace `{{baseUrl}}` with your API base URL (e.g., `http://localhost:8080`)
- Replace `{{hospitalId}}` with the actual hospital ID (e.g., `11`)
- Replace `{{accessToken}}` with a valid JWT token
- All optional fields can be omitted if not needed
- Multiple emergency contacts can be added in the array
- Only one emergency contact should have `isPrimary: true`

