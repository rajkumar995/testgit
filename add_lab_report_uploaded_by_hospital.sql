-- Add uploaded_by_hospital_id to lab reports (null = patient self-upload; non-null = uploaded by that hospital)
-- Run this if your DB was created before this column was added (JPA ddl-auto=update may add it automatically).

ALTER TABLE md_lab_reports ADD COLUMN IF NOT EXISTS uploaded_by_hospital_id BIGINT NULL;
CREATE INDEX IF NOT EXISTS idx_uploaded_by_hospital_id ON md_lab_reports(uploaded_by_hospital_id);
