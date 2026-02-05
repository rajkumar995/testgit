-- Permission requests: doctor/hospital requests access to patient reports/vitals/health
CREATE TABLE md_permission_requests (
    id BIGSERIAL PRIMARY KEY,
    global_patient_id BIGINT NOT NULL,
    doctor_id BIGINT NULL,
    hospital_id BIGINT NULL,
    scope VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NULL,
    updated_at TIMESTAMP NULL
);
CREATE INDEX idx_pr_patient ON md_permission_requests(global_patient_id);
CREATE INDEX idx_pr_doctor ON md_permission_requests(doctor_id);
CREATE INDEX idx_pr_hospital ON md_permission_requests(hospital_id);
CREATE INDEX idx_pr_status ON md_permission_requests(status);
