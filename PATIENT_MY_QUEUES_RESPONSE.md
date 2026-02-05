# Patient "My Queues" API – Response

**Endpoint:** `GET /api/v1/queues/my-queues`  
**Auth:** Bearer token (PATIENT)  
**Response:** `200 OK` – array of queue items.

---

## Response body (array of queue items)

Each element has the same shape. All fields that can be present are listed below.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Queue record ID |
| `bookingDate` | String (date) | Date of the appointment (e.g. `"2025-02-15"`) |
| `doctorId` | Long | Doctor ID |
| `doctorName` | String | Doctor name |
| `hospitalId` | Long | Hospital ID |
| `hospitalName` | String | Hospital name |
| `queueNumber` | Integer | This patient’s queue number |
| `status` | String | This patient’s queue status: `WAITING`, `CALLED`, `SERVING`, `SERVED`, `CANCELLED` |
| `position` | Integer | How many patients are ahead (0 if currently serving/called) |
| `estimatedWaitTime` | Integer | Estimated wait in **minutes** (from position × avg consultation time) |
| `currentServingQueueNumber` | Integer | Queue number of the patient currently being served (null if none) |
| `currentServingPatientStatus` | String | Status of current serving (e.g. `"SERVING"`), or null |
| `bookingId` | Long | Booking ID |
| `patientId` | Long | Hospital patient ID (for this patient) |
| `patientName` | String | Patient name (for this patient) |
| `patientPhone` | String | Patient phone (for this patient) |

---

## Example response (JSON)

```json
[
  {
    "id": 16,
    "bookingDate": "2025-02-15",
    "doctorId": 1,
    "doctorName": "Dr. Smith",
    "hospitalId": 1,
    "hospitalName": "City Hospital",
    "queueNumber": 6,
    "status": "WAITING",
    "position": 2,
    "estimatedWaitTime": 30,
    "currentServingQueueNumber": 4,
    "currentServingPatientStatus": "SERVING",
    "bookingId": 42,
    "patientId": 101,
    "patientName": "John Doe",
    "patientPhone": "+1234567890"
  },
  {
    "id": 18,
    "bookingDate": "2025-02-16",
    "doctorId": 2,
    "doctorName": "Dr. Jane Lee",
    "hospitalId": 1,
    "hospitalName": "City Hospital",
    "queueNumber": 1,
    "status": "CALLED",
    "position": 0,
    "estimatedWaitTime": 0,
    "currentServingQueueNumber": null,
    "currentServingPatientStatus": null,
    "bookingId": 44,
    "patientId": 101,
    "patientName": "John Doe",
    "patientPhone": "+1234567890"
  }
]
```

---

## Notes

- **Empty list:** `[]` when the patient has no queues.
- **Current serving:** When no one is being served yet, `currentServingQueueNumber` and `currentServingPatientStatus` are `null`.
- **Dates:** `bookingDate` is ISO date (`yyyy-MM-dd`).
- **Status enum:** `WAITING` → `CALLED` → `SERVING` → `SERVED`; or `CANCELLED` at any time.
