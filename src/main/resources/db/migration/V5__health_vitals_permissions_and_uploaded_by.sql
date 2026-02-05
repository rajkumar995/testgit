-- Health and Vitals permissions (same flow as report permissions)
-- When patient approves HEALTH/VITALS request, hospital/doctor can see all patient + hospital data

CREATE TABLE md_health_permissions (
    id BIGSERIAL PRIMARY KEY,
    global_patient_id BIGINT NOT NULL,
    doctor_id BIGINT NULL,
    hospital_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT UK_health_permission UNIQUE (global_patient_id, doctor_id, hospital_id)
);
CREATE INDEX idx_hp_global_patient_id ON md_health_permissions(global_patient_id);
CREATE INDEX idx_hp_doctor_id ON md_health_permissions(doctor_id);
CREATE INDEX idx_hp_hospital_id ON md_health_permissions(hospital_id);

CREATE TABLE md_vitals_permissions (
    id BIGSERIAL PRIMARY KEY,
    global_patient_id BIGINT NOT NULL,
    doctor_id BIGINT NULL,
    hospital_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NULL,
    updated_at TIMESTAMP NULL,
    CONSTRAINT UK_vitals_permission UNIQUE (global_patient_id, doctor_id, hospital_id)
);
CREATE INDEX idx_vp_global_patient_id ON md_vitals_permissions(global_patient_id);
CREATE INDEX idx_vp_doctor_id ON md_vitals_permissions(doctor_id);
CREATE INDEX idx_vp_hospital_id ON md_vitals_permissions(hospital_id);

-- Track who uploaded/created health profile and vitals (null = patient)
ALTER TABLE md_health_profiles
    ADD COLUMN uploaded_by_hospital_id BIGINT NULL,
    ADD COLUMN uploaded_by_doctor_id BIGINT NULL;

ALTER TABLE md_vitals_records
    ADD COLUMN uploaded_by_hospital_id BIGINT NULL,
    ADD COLUMN uploaded_by_doctor_id BIGINT NULL;

CREATE INDEX idx_health_profiles_uploaded_by_hospital ON md_health_profiles(uploaded_by_hospital_id);
CREATE INDEX idx_vitals_records_uploaded_by_hospital ON md_vitals_records(uploaded_by_hospital_id);
