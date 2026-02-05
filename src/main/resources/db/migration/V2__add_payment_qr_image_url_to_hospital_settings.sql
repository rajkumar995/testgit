-- Add payment_qr_image_url to md_hospital_settings for QR image URL (e.g. UPI QR) shown on invoice.
-- Run when using spring.jpa.hibernate.ddl-auto=validate (e.g. production).
ALTER TABLE md_hospital_settings ADD COLUMN IF NOT EXISTS payment_qr_image_url VARCHAR(1024) NULL;
