# Hospital Registration - User Creation Guide

## Overview
When a hospital is registered, an admin user account is automatically created that can be used to login to the system.

## Changes Made

### 1. Fixed Security Configuration Pattern Error
**Issue**: Invalid pattern `/api/v1/queues/**/live/public` causing `PatternParseException`

**Fix**: Changed to `/api/v1/queues/live/doctor/*/public` (using single wildcard `*` instead of double `**`)

### 2. Added User Creation During Hospital Registration

#### Updated `HospitalRegistrationRequest`
Added two new required fields:
- `adminUsername`: Email or phone number for login (must be unique)
- `adminPassword`: Password for the admin account (will be encrypted)

#### Updated `HospitalServiceImpl`
- Creates a `User` account when hospital is registered
- Role assigned: `HOSPITAL_STAFF` (hospital admin can manage queues and bookings)
- Password is encrypted using BCrypt
- Username uniqueness is validated
- Username is returned in the response (for confirmation)

#### Updated `HospitalResponse`
- Added `adminUsername` field to return the created username in the response

## API Usage

### Register Hospital Request
```json
{
    "name": "City General Hospital",
    "founder": "Dr. John Smith",
    "foundedOn": "1990-01-15",
    "images": ["https://example.com/hospital-image1.jpg"],
    "address": {
        "addressLine1": "123 Medical Street",
        "addressLine2": "Block A",
        "city": "Mumbai",
        "state": "Maharashtra",
        "country": "India",
        "pincode": "400001",
        "latitude": 19.0760,
        "longitude": 72.8777
    },
    "emergencyAvailable": true,
    "country": "India",
    "services": ["Emergency Care", "Cardiology"],
    "facilities": ["24/7 Emergency", "ICU"],
    "emergencyCallNumber": "+91-22-12345678",
    "bookingCallNumber": "+91-22-87654321",
    "adminUsername": "hospital.admin@cityhospital.com",
    "adminPassword": "SecurePassword123!"
}
```

### Register Hospital Response
```json
{
    "id": 1,
    "name": "City General Hospital",
    "founder": "Dr. John Smith",
    "foundedOn": "1990-01-15",
    "images": ["https://example.com/hospital-image1.jpg"],
    "address": {
        "id": 1,
        "addressLine1": "123 Medical Street",
        "city": "Mumbai",
        "state": "Maharashtra",
        "country": "India",
        "pincode": "400001"
    },
    "emergencyAvailable": true,
    "country": "India",
    "services": ["Emergency Care", "Cardiology"],
    "facilities": ["24/7 Emergency", "ICU"],
    "isActive": true,
    "adminUsername": "hospital.admin@cityhospital.com"
}
```

## Login After Registration

After successful hospital registration, the admin can login using:

**Endpoint**: `POST /api/v1/auth/login`

**Request**:
```json
{
    "username": "hospital.admin@cityhospital.com",
    "password": "SecurePassword123!"
}
```

**Response**:
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "role": "HOSPITAL_STAFF",
    "userId": 1
}
```

## User Role and Permissions

The created hospital admin user has the `HOSPITAL_STAFF` role, which allows:
- ✅ Manage queues (update queue status)
- ✅ View queues (full queue details)
- ✅ View bookings for the hospital
- ✅ View patients
- ✅ Change queue status

## Error Handling

### Username Already Exists
If the `adminUsername` already exists in the system:
```json
{
    "error": "Username already exists: hospital.admin@cityhospital.com"
}
```

### Missing Required Fields
If `adminUsername` or `adminPassword` is missing:
```json
{
    "error": "Admin username is required"
}
```
or
```json
{
    "error": "Admin password is required"
}
```

## Security Notes

1. **Password Encryption**: Passwords are automatically encrypted using BCrypt before storage
2. **Username Uniqueness**: Username must be unique across all users
3. **First Login Flag**: New users have `isFirstLogin = true` (can be used to force password change)
4. **Active Status**: New users are created with `isActive = true`

## Postman Collection Update

The Postman collection has been updated to include the new fields:
- `adminUsername` in the Register Hospital request body
- `adminPassword` in the Register Hospital request body

## Testing Flow

1. **Register Hospital** → Get `adminUsername` in response
2. **Login** → Use `adminUsername` and `adminPassword` to get JWT tokens
3. **Use Tokens** → Access protected endpoints with the JWT token

## Example Complete Flow

```bash
# 1. Register Hospital
POST /api/v1/hospitals
{
    "name": "City Hospital",
    "adminUsername": "admin@cityhospital.com",
    "adminPassword": "Admin123!",
    ...
}

# Response includes:
{
    "id": 1,
    "adminUsername": "admin@cityhospital.com",
    ...
}

# 2. Login
POST /api/v1/auth/login
{
    "username": "admin@cityhospital.com",
    "password": "Admin123!"
}

# 3. Use token for authenticated requests
GET /api/v1/queues/full/doctor/1?date=2024-12-25
Authorization: Bearer <accessToken>
```
