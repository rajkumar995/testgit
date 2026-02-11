-- Create pharmacy counters table for tracking counter sessions
-- Run this when using spring.jpa.hibernate.ddl-auto=validate (e.g. production).
-- For development with ddl-auto=update, Hibernate will create this automatically.

CREATE TABLE IF NOT EXISTS md_pharmacy_counters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    counter_number VARCHAR(50),
    opened_at DATETIME NOT NULL,
    closed_at DATETIME,
    opening_amount DECIMAL(19, 2),
    closing_amount DECIMAL(19, 2),
    total_sales DECIMAL(19, 2),
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    opened_by VARCHAR(255),
    closed_by VARCHAR(255),
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255),
    updated_at DATETIME,
    FOREIGN KEY (hospital_id) REFERENCES md_hospitals(id) ON DELETE CASCADE,
    INDEX idx_counter_hospital (hospital_id),
    INDEX idx_counter_status (hospital_id, is_open)
);
