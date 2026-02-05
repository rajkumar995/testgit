-- Fix Booking table: Remove old patient_id column
-- This column was replaced by hospital_patient_id and global_patient_id
-- 
-- IMPORTANT: Run this SQL script manually in your MySQL database
-- The old patient_id column is causing errors because it's NOT NULL but not being set

-- Step 1: First, make the column nullable (if it exists)
-- This will prevent the error temporarily
ALTER TABLE md_bookings MODIFY COLUMN patient_id BIGINT NULL;

-- Step 2: Drop the old patient_id column (safe to run even if column doesn't exist)
-- Note: If you get "Unknown column" error, the column is already removed
ALTER TABLE md_bookings DROP COLUMN patient_id;

-- Step 3: Verify the table structure
DESCRIBE md_bookings;

-- Expected columns after fix:
-- - id
-- - doctor_id
-- - hospital_patient_id (NEW)
-- - global_patient_id (NEW)
-- - hospital_id
-- - booking_date
-- - booking_time
-- - phone_number
-- - payment_id
-- - status
-- - created_at, created_by, updated_at
-- - patient_id should be REMOVED
