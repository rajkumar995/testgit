-- Fix role column size to accommodate HOSPITAL_ADMIN (14 characters)
-- Run this SQL script on your MySQL database

ALTER TABLE md_users MODIFY COLUMN role VARCHAR(20) NOT NULL;

-- Verify the change
DESCRIBE md_users;
