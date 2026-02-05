# MediDropBox API Postman Collection

## Overview
This Postman collection contains all the APIs for the MediDropBox application including hospital registration, doctor management, booking, queue management, and authentication.

## Import Instructions

1. Open Postman
2. Click on **Import** button (top left)
3. Select the file: `MediDropBox-API.postman_collection.json`
4. The collection will be imported with all endpoints organized by category

## Environment Variables

The collection uses the following variables. You can set them in Postman Environment or Collection Variables:

- `baseUrl`: Base URL of your API (default: `http://localhost:8080`)
- `accessToken`: JWT access token (auto-set after login)
- `refreshToken`: JWT refresh token (auto-set after login)
- `hospitalId`: Hospital ID for testing (default: `1`)
- `doctorId`: Doctor ID for testing (default: `1`)
- `bookingId`: Booking ID for testing (default: `1`)
- `queueId`: Queue ID for testing (default: `1`)
- `shareToken`: Share token for booking share (set manually)
- `shareId`: Share ID for revoking shares (default: `1`)

### Setting Up Environment Variables

1. In Postman, click on **Environments** (left sidebar)
2. Create a new environment or use **Globals**
3. Add the variables listed above
4. Select the environment before running requests

## Collection Structure

### 1. Authentication
- **Login**: Authenticate and get JWT tokens (auto-saves tokens)
- **Refresh Token**: Get new access token using refresh token
- **Reset Password**: Reset user password

### 2. Hospital Management
- **Register Hospital**: Public endpoint to register a new hospital
- **Get Hospital by ID**: Get hospital details (authenticated)
- **Get Hospital by ID (Public)**: Public endpoint
- **Get All Hospitals**: List all active hospitals
- **Update Hospital**: Update hospital details (ADMIN/PRODUCT_ADMIN/SUPER_ADMIN only)
- **Deactivate Hospital**: Deactivate a hospital (ADMIN/PRODUCT_ADMIN/SUPER_ADMIN only)

### 3. Doctor Management
- **Create Doctor**: Create a new doctor (ADMIN/PRODUCT_ADMIN/SUPER_ADMIN only)
- **Get Doctor by ID**: Get doctor details (authenticated)
- **Get Doctor by ID (Public)**: Public endpoint
- **Get Doctors by Hospital**: Get all doctors for a hospital
- **Get Doctors by Hospital (Public)**: Public endpoint
- **Update Doctor**: Update doctor details (Doctor can update self, ADMIN can update any)

### 4. Booking Management
- **Create Booking**: Create a new booking (PATIENT only)
- **Get My Bookings**: Get all bookings for current patient (PATIENT only)
- **Get Bookings by Hospital**: Get all bookings for a hospital (STAFF/ADMIN only)
- **Get Bookings by Doctor**: Get all bookings for a doctor (STAFF/DOCTOR/ADMIN only)
- **Get Booking by ID**: Get booking details with role-based filtering

### 5. Queue Management
- **Get Live Queue**: Get live queue for a doctor (authenticated)
- **Get Live Queue (Public)**: Public endpoint
- **Get Full Queue**: Get full queue details (STAFF/DOCTOR/ADMIN only)
- **Update Queue Status**: Update queue status (STAFF/ADMIN only)
  - Status values: `WAITING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- **Get My Queues**: Get all queues for current patient (PATIENT only)

### 6. Booking Share
- **Share Booking**: Share a booking with friends/family (PATIENT only)
- **Get Shared Booking**: Get booking details using share token (Public)
- **Revoke Share**: Revoke a booking share (PATIENT only)

## Usage Flow

### Step 1: Authentication
1. Use the **Login** endpoint with valid credentials
2. The access token and refresh token will be automatically saved to variables
3. All subsequent requests will use the access token

### Step 2: Register Hospital (Public)
1. Use **Register Hospital** endpoint (no authentication required)
2. Copy the returned `hospitalId` and update the `hospitalId` variable

### Step 3: Create Doctor (Requires Admin Token)
1. Ensure you're logged in as ADMIN/PRODUCT_ADMIN/SUPER_ADMIN
2. Use **Create Doctor** endpoint
3. Set the `hospitalId` in the request body
4. Copy the returned `doctorId` and update the `doctorId` variable

### Step 4: Create Booking (Requires Patient Token)
1. Login as a PATIENT user
2. Use **Create Booking** endpoint
3. Set `doctorId`, `patientId`, `hospitalId`, `bookingDate`, and `bookingTime`
4. Copy the returned `bookingId` and update the `bookingId` variable

### Step 5: Manage Queue (Requires Staff/Admin Token)
1. Login as HOSPITAL_STAFF or ADMIN
2. Use **Get Live Queue** or **Get Full Queue** to view queues
3. Use **Update Queue Status** to change queue status

## Sample Request Bodies

### Register Hospital
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
    "bookingCallNumber": "+91-22-87654321"
}
```

### Create Doctor
```json
{
    "name": "Dr. Jane Doe",
    "title": "MD, MBBS",
    "specialty": "Cardiology",
    "phone": "+91-9876543210",
    "email": "dr.jane@hospital.com",
    "hospitalId": 1,
    "fees": 1500.00,
    "averageConsultationTime": 30,
    "allowRemote": true
}
```

### Create Booking
```json
{
    "doctorId": 1,
    "patientId": 1,
    "hospitalId": 1,
    "bookingDate": "2024-12-25",
    "bookingTime": "10:00:00",
    "phoneNumber": "+91-9876543210"
}
```

## Role-Based Access

- **PUBLIC**: Can view public endpoints (hospitals, doctors, queues)
- **PATIENT**: Can create bookings, view own bookings/queues, share bookings
- **DOCTOR**: Can view own bookings/queues, update own profile
- **HOSPITAL_STAFF**: Can manage queues, view hospital bookings
- **PRODUCT_ADMIN**: Can manage hospitals and doctors
- **ADMIN**: Full access to all resources
- **SUPER_ADMIN**: Full access to all resources

## Notes

1. **Auto Token Management**: The Login endpoint automatically saves tokens to variables
2. **Bearer Token**: Most endpoints require Bearer token authentication
3. **Date Format**: Use `YYYY-MM-DD` format for dates
4. **Time Format**: Use `HH:mm:ss` format for times (24-hour)
5. **Public Endpoints**: Some endpoints have `/public` variants that don't require authentication
6. **Role Filtering**: Responses are filtered based on user role (e.g., PUBLIC users won't see phone/email)

## Troubleshooting

### Token Expired
- Use the **Refresh Token** endpoint to get a new access token
- Or login again using the **Login** endpoint

### 401 Unauthorized
- Check if the access token is set correctly
- Ensure you're using the correct role for the endpoint
- Try refreshing the token or logging in again

### 403 Forbidden
- Verify you have the required role/permission for the endpoint
- Check the endpoint documentation for required roles

### 404 Not Found
- Verify the IDs (hospitalId, doctorId, etc.) are correct
- Check if the resource exists in the database

## Base URL Configuration

Default base URL is `http://localhost:8080`. To change it:
1. Update the `baseUrl` variable in Postman
2. Or set it in your environment variables

For production, change to your production URL:
```
https://api.medidropbox.com
```
