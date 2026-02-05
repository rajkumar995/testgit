-- Fix all tables: Remove old patient_id columns
-- These columns were replaced by hospital_patient_id and global_patient_id
-- 
-- IMPORTANT: Run this SQL script manually in your MySQL database
-- The old patient_id columns are causing errors because they're NOT NULL but not being set

-- ============================================
-- Fix md_bookings table
-- ============================================
-- Step 1: Make column nullable first (prevents error)
ALTER TABLE md_bookings MODIFY COLUMN patient_id BIGINT NULL;

-- Step 2: Drop the old patient_id column
ALTER TABLE md_bookings DROP COLUMN patient_id;
-- Note: If you get "Unknown column" error, the column is already removed

-- ============================================
-- Fix md_queues table
-- ============================================
-- Step 1: Make column nullable first (prevents error)
ALTER TABLE md_queues MODIFY COLUMN patient_id BIGINT NULL;

-- Step 2: Drop the old patient_id column
ALTER TABLE md_queues DROP COLUMN patient_id;

-- ============================================
-- Verify the table structures
-- ============================================
DESCRIBE md_bookings;
DESCRIBE md_queues;

-- Expected columns after fix:
-- md_bookings should have: hospital_patient_id, global_patient_id (NOT patient_id)
-- md_queues should have: hospital_patient_id, global_patient_id (NOT patient_id)
