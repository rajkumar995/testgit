-- Add invoice_url to md_payments for storing S3 URL of generated invoice PDF.
-- Run this when using spring.jpa.hibernate.ddl-auto=validate (e.g. production).
ALTER TABLE md_payments ADD COLUMN IF NOT EXISTS invoice_url VARCHAR(1024) NULL;
