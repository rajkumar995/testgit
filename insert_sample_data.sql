-- ============================================
-- MediDropBox Sample Data Insert Script
-- Database: Smartbecho-3530343779fe
-- ============================================
-- This script inserts sample data for:
-- 1. Addresses (for Hospital, Doctor, and Patients)
-- 2. Hospitals (with Services, Facilities, Images, Social Media Links)
-- 3. Awards (for Doctors)
-- 4. Doctors (with Services, Expertise, Languages, Awards)
-- 5. Global Patients
-- 6. Global Patient Emergency Contacts
-- 7. Hospital Patients
-- 8. Hospital Patient Emergency Contacts
-- ============================================

USE `Smartbecho-3530343779fe`;

-- ============================================
-- 1. INSERT ADDRESSES
-- ============================================
-- Addresses for Hospitals
INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('123 Medical Center Drive', 'Building A, Floor 2', 'Mumbai', 'Maharashtra', 'India', '400001', 19.0760, 72.8777, 'https://maps.google.com/?q=19.0760,72.8777', NOW(), NOW(), 'system');

SET @hospital_address_1 = LAST_INSERT_ID();

INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('456 Health Care Avenue', 'Sector 5', 'Delhi', 'Delhi', 'India', '110001', 28.6139, 77.2090, 'https://maps.google.com/?q=28.6139,77.2090', NOW(), NOW(), 'system');

SET @hospital_address_2 = LAST_INSERT_ID();

INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('789 Hospital Road', 'Near City Park', 'Bangalore', 'Karnataka', 'India', '560001', 12.9716, 77.5946, 'https://maps.google.com/?q=12.9716,77.5946', NOW(), NOW(), 'system');

SET @hospital_address_3 = LAST_INSERT_ID();

-- Addresses for Doctors
INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('101 Doctor Lane', 'Apartment 5B', 'Mumbai', 'Maharashtra', 'India', '400002', 19.0759, 72.8776, 'https://maps.google.com/?q=19.0759,72.8776', NOW(), NOW(), 'system');

SET @doctor_address_1 = LAST_INSERT_ID();

INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('202 Medical Plaza', 'Suite 301', 'Delhi', 'Delhi', 'India', '110002', 28.6140, 77.2091, 'https://maps.google.com/?q=28.6140,77.2091', NOW(), NOW(), 'system');

SET @doctor_address_2 = LAST_INSERT_ID();

INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('303 Health Street', 'Block C', 'Bangalore', 'Karnataka', 'India', '560002', 12.9717, 77.5947, 'https://maps.google.com/?q=12.9717,77.5947', NOW(), NOW(), 'system');

SET @doctor_address_3 = LAST_INSERT_ID();

-- Addresses for Patients (Global and Hospital)
INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('501 Patient Street', 'House No. 10', 'Mumbai', 'Maharashtra', 'India', '400003', 19.0758, 72.8775, 'https://maps.google.com/?q=19.0758,72.8775', NOW(), NOW(), 'system');

SET @patient_address_1 = LAST_INSERT_ID();

INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('602 Residential Area', 'Flat 202', 'Delhi', 'Delhi', 'India', '110003', 28.6141, 77.2092, 'https://maps.google.com/?q=28.6141,77.2092', NOW(), NOW(), 'system');

SET @patient_address_2 = LAST_INSERT_ID();

INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('703 Home Avenue', 'Building 3', 'Bangalore', 'Karnataka', 'India', '560003', 12.9718, 77.5948, 'https://maps.google.com/?q=12.9718,77.5948', NOW(), NOW(), 'system');

SET @patient_address_3 = LAST_INSERT_ID();

INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('804 Living Quarters', 'Unit 15', 'Mumbai', 'Maharashtra', 'India', '400004', 19.0757, 72.8774, 'https://maps.google.com/?q=19.0757,72.8774', NOW(), NOW(), 'system');

SET @patient_address_4 = LAST_INSERT_ID();

INSERT INTO md_addresses (address_line1, address_line2, city, state, country, pincode, latitude, longitude, location_url, created_at, updated_at, created_by) VALUES
('905 Residence Road', 'Apartment 8A', 'Delhi', 'Delhi', 'India', '110004', 28.6142, 77.2093, 'https://maps.google.com/?q=28.6142,77.2093', NOW(), NOW(), 'system');

SET @patient_address_5 = LAST_INSERT_ID();

-- ============================================
-- 2. INSERT HOSPITALS
-- ============================================
INSERT INTO md_hospitals (name, founder, founded_on, address_id, emergency_available, country, emergency_call_number, booking_call_number, is_active, created_at, updated_at, created_by) VALUES
('City General Hospital', 'Dr. Rajesh Kumar', '2010-01-15', @hospital_address_1, TRUE, 'India', '+91-9876543210', '+91-9876543211', TRUE, NOW(), NOW(), 'system');

SET @hospital_1 = LAST_INSERT_ID();

INSERT INTO md_hospitals (name, founder, founded_on, address_id, emergency_available, country, emergency_call_number, booking_call_number, is_active, created_at, updated_at, created_by) VALUES
('Metro Health Care', 'Dr. Priya Sharma', '2015-03-20', @hospital_address_2, TRUE, 'India', '+91-9876543220', '+91-9876543221', TRUE, NOW(), NOW(), 'system');

SET @hospital_2 = LAST_INSERT_ID();

INSERT INTO md_hospitals (name, founder, founded_on, address_id, emergency_available, country, emergency_call_number, booking_call_number, is_active, created_at, updated_at, created_by) VALUES
('Sunshine Medical Center', 'Dr. Amit Patel', '2018-06-10', @hospital_address_3, TRUE, 'India', '+91-9876543230', '+91-9876543231', TRUE, NOW(), NOW(), 'system');

SET @hospital_3 = LAST_INSERT_ID();

-- Insert Hospital Services
INSERT INTO md_hospital_services (hospital_id, service) VALUES
(@hospital_1, 'Emergency Care'),
(@hospital_1, 'Cardiology'),
(@hospital_1, 'Orthopedics'),
(@hospital_1, 'Pediatrics'),
(@hospital_1, 'General Surgery'),
(@hospital_2, 'Emergency Care'),
(@hospital_2, 'Neurology'),
(@hospital_2, 'Oncology'),
(@hospital_2, 'Dermatology'),
(@hospital_2, 'Gynecology'),
(@hospital_3, 'Emergency Care'),
(@hospital_3, 'Cardiology'),
(@hospital_3, 'ENT'),
(@hospital_3, 'Ophthalmology'),
(@hospital_3, 'Psychiatry');

-- Insert Hospital Facilities
INSERT INTO md_hospital_facilities (hospital_id, facility) VALUES
(@hospital_1, 'ICU'),
(@hospital_1, 'Operation Theater'),
(@hospital_1, 'Laboratory'),
(@hospital_1, 'Pharmacy'),
(@hospital_1, 'Ambulance Service'),
(@hospital_1, 'X-Ray'),
(@hospital_1, 'MRI'),
(@hospital_2, 'ICU'),
(@hospital_2, 'Operation Theater'),
(@hospital_2, 'Laboratory'),
(@hospital_2, 'Pharmacy'),
(@hospital_2, 'Ambulance Service'),
(@hospital_2, 'CT Scan'),
(@hospital_2, 'Ultrasound'),
(@hospital_3, 'ICU'),
(@hospital_3, 'Operation Theater'),
(@hospital_3, 'Laboratory'),
(@hospital_3, 'Pharmacy'),
(@hospital_3, 'Ambulance Service'),
(@hospital_3, 'X-Ray'),
(@hospital_3, 'ECG');

-- Insert Hospital Images
INSERT INTO md_hospital_images (hospital_id, image_url) VALUES
(@hospital_1, 'https://example.com/hospital1-image1.jpg'),
(@hospital_1, 'https://example.com/hospital1-image2.jpg'),
(@hospital_2, 'https://example.com/hospital2-image1.jpg'),
(@hospital_2, 'https://example.com/hospital2-image2.jpg'),
(@hospital_3, 'https://example.com/hospital3-image1.jpg'),
(@hospital_3, 'https://example.com/hospital3-image2.jpg');

-- Insert Social Media Links
INSERT INTO md_social_media_links (platform_name, profile_url, created_at, updated_at, created_by) VALUES
('Facebook', 'https://facebook.com/citygeneralhospital', NOW(), NOW(), 'system');

SET @social_media_1 = LAST_INSERT_ID();

INSERT INTO md_social_media_links (platform_name, profile_url, created_at, updated_at, created_by) VALUES
('Twitter', 'https://twitter.com/citygeneralhospital', NOW(), NOW(), 'system');

SET @social_media_2 = LAST_INSERT_ID();

INSERT INTO md_social_media_links (platform_name, profile_url, created_at, updated_at, created_by) VALUES
('LinkedIn', 'https://linkedin.com/company/citygeneralhospital', NOW(), NOW(), 'system');

SET @social_media_3 = LAST_INSERT_ID();

INSERT INTO md_social_media_links (platform_name, profile_url, created_at, updated_at, created_by) VALUES
('Instagram', 'https://instagram.com/metrohealthcare', NOW(), NOW(), 'system');

SET @social_media_4 = LAST_INSERT_ID();

INSERT INTO md_social_media_links (platform_name, profile_url, created_at, updated_at, created_by) VALUES
('Facebook', 'https://facebook.com/metrohealthcare', NOW(), NOW(), 'system');

SET @social_media_5 = LAST_INSERT_ID();

INSERT INTO md_social_media_links (platform_name, profile_url, created_at, updated_at, created_by) VALUES
('YouTube', 'https://youtube.com/metrohealthcare', NOW(), NOW(), 'system');

SET @social_media_6 = LAST_INSERT_ID();

INSERT INTO md_social_media_links (platform_name, profile_url, created_at, updated_at, created_by) VALUES
('Facebook', 'https://facebook.com/sunshinemedical', NOW(), NOW(), 'system');

SET @social_media_7 = LAST_INSERT_ID();

INSERT INTO md_social_media_links (platform_name, profile_url, created_at, updated_at, created_by) VALUES
('Instagram', 'https://instagram.com/sunshinemedical', NOW(), NOW(), 'system');

SET @social_media_8 = LAST_INSERT_ID();

-- Link Social Media to Hospitals (md_hospital_social_media join table)
INSERT INTO md_hospital_social_media (hospital_id, social_media_link_id) VALUES
(@hospital_1, @social_media_1),
(@hospital_1, @social_media_2),
(@hospital_1, @social_media_3),
(@hospital_2, @social_media_4),
(@hospital_2, @social_media_5),
(@hospital_2, @social_media_6),
(@hospital_3, @social_media_7),
(@hospital_3, @social_media_8);

-- ============================================
-- 3. INSERT AWARDS (for Doctors)
-- ============================================
INSERT INTO md_awards (award_name, awarded_by, award_year, award_image_url, created_at, updated_at, created_by) VALUES
('Best Cardiologist Award', 'Medical Association of India', 2023, 'https://example.com/award1.jpg', NOW(), NOW(), 'system');

SET @award_1 = LAST_INSERT_ID();

INSERT INTO md_awards (award_name, awarded_by, award_year, award_image_url, created_at, updated_at, created_by) VALUES
('Excellence in Cardiology', 'Cardiac Society', 2022, 'https://example.com/award2.jpg', NOW(), NOW(), 'system');

SET @award_2 = LAST_INSERT_ID();

INSERT INTO md_awards (award_name, awarded_by, award_year, award_image_url, created_at, updated_at, created_by) VALUES
('Outstanding Neurologist', 'Neurological Society', 2023, 'https://example.com/award3.jpg', NOW(), NOW(), 'system');

SET @award_3 = LAST_INSERT_ID();

INSERT INTO md_awards (award_name, awarded_by, award_year, award_image_url, created_at, updated_at, created_by) VALUES
('Best Orthopedic Surgeon', 'Orthopedic Association', 2022, 'https://example.com/award4.jpg', NOW(), NOW(), 'system');

SET @award_4 = LAST_INSERT_ID();

INSERT INTO md_awards (award_name, awarded_by, award_year, award_image_url, created_at, updated_at, created_by) VALUES
('Patient Choice Award', 'Healthcare Excellence', 2023, 'https://example.com/award5.jpg', NOW(), NOW(), 'system');

SET @award_5 = LAST_INSERT_ID();

-- ============================================
-- 4. INSERT DOCTORS
-- ============================================
INSERT INTO md_doctors (name, title, about, specialty, phone, email, address_id, served_patient_count, rating, profile_photo_url, is_active, fees, average_consultation_time, allow_remote, hospital_id, created_at, updated_at, created_by) VALUES
('Dr. Anil Verma', 'MBBS, MD', 'Experienced cardiologist with 15 years of practice. Specialized in heart diseases and cardiac surgery.', 'Cardiology', '+91-9876543301', 'anil.verma@hospital.com', @doctor_address_1, 1500, 4.8, 'https://example.com/doctor1-photo.jpg', TRUE, 1500.00, 30, TRUE, @hospital_1, NOW(), NOW(), 'system');

SET @doctor_1 = LAST_INSERT_ID();

INSERT INTO md_doctors (name, title, about, specialty, phone, email, address_id, served_patient_count, rating, profile_photo_url, is_active, fees, average_consultation_time, allow_remote, hospital_id, created_at, updated_at, created_by) VALUES
('Dr. Sunita Reddy', 'MBBS, MS', 'Renowned neurologist specializing in brain and nervous system disorders. 12 years of experience.', 'Neurology', '+91-9876543302', 'sunita.reddy@hospital.com', @doctor_address_2, 1200, 4.7, 'https://example.com/doctor2-photo.jpg', TRUE, 2000.00, 45, FALSE, @hospital_2, NOW(), NOW(), 'system');

SET @doctor_2 = LAST_INSERT_ID();

INSERT INTO md_doctors (name, title, about, specialty, phone, email, address_id, served_patient_count, rating, profile_photo_url, is_active, fees, average_consultation_time, allow_remote, hospital_id, created_at, updated_at, created_by) VALUES
('Dr. Ravi Kumar', 'MBBS, DNB', 'Expert orthopedic surgeon with expertise in joint replacement and sports medicine.', 'Orthopedics', '+91-9876543303', 'ravi.kumar@hospital.com', @doctor_address_3, 1800, 4.9, 'https://example.com/doctor3-photo.jpg', TRUE, 1800.00, 25, TRUE, @hospital_3, NOW(), NOW(), 'system');

SET @doctor_3 = LAST_INSERT_ID();

-- Insert Doctor Services
INSERT INTO md_doctor_services (doctor_id, service) VALUES
(@doctor_1, 'ECG'),
(@doctor_1, 'Echocardiography'),
(@doctor_1, 'Stress Test'),
(@doctor_1, 'Cardiac Consultation'),
(@doctor_2, 'EEG'),
(@doctor_2, 'MRI Brain'),
(@doctor_2, 'Neurological Consultation'),
(@doctor_2, 'Epilepsy Treatment'),
(@doctor_3, 'X-Ray'),
(@doctor_3, 'Joint Replacement'),
(@doctor_3, 'Sports Injury Treatment'),
(@doctor_3, 'Orthopedic Consultation');

-- Insert Doctor Expertise
INSERT INTO md_doctor_expertise (doctor_id, expertise) VALUES
(@doctor_1, 'Heart Disease'),
(@doctor_1, 'Cardiac Surgery'),
(@doctor_1, 'Arrhythmia'),
(@doctor_1, 'Hypertension'),
(@doctor_2, 'Epilepsy'),
(@doctor_2, 'Stroke'),
(@doctor_2, 'Parkinson Disease'),
(@doctor_2, 'Migraine'),
(@doctor_3, 'Knee Replacement'),
(@doctor_3, 'Hip Replacement'),
(@doctor_3, 'Sports Medicine'),
(@doctor_3, 'Fracture Treatment');

-- Insert Doctor Languages
INSERT INTO md_doctor_languages (doctor_id, language) VALUES
(@doctor_1, 'English'),
(@doctor_1, 'Hindi'),
(@doctor_1, 'Marathi'),
(@doctor_2, 'English'),
(@doctor_2, 'Hindi'),
(@doctor_2, 'Telugu'),
(@doctor_3, 'English'),
(@doctor_3, 'Hindi'),
(@doctor_3, 'Kannada');

-- Link Awards to Doctors (md_doctor_awards join table)
INSERT INTO md_doctor_awards (doctor_id, award_id) VALUES
(@doctor_1, @award_1),
(@doctor_1, @award_2),
(@doctor_1, @award_5),
(@doctor_2, @award_3),
(@doctor_2, @award_5),
(@doctor_3, @award_4),
(@doctor_3, @award_5);

-- ============================================
-- 5. INSERT GLOBAL PATIENTS
-- ============================================
INSERT INTO md_global_patients (full_name, phone, email, profile_image_url, is_active, status, aadhar_id, abha_id, address_id, created_at, updated_at, created_by) VALUES
('Rajesh Kumar', '+91-9876543401', 'rajesh.kumar@email.com', 'https://example.com/patient1-photo.jpg', TRUE, 'CLAIMED', '123456789012', 'ABHA1234567890', @patient_address_1, NOW(), NOW(), 'system');

SET @global_patient_1 = LAST_INSERT_ID();

INSERT INTO md_global_patients (full_name, phone, email, profile_image_url, is_active, status, aadhar_id, abha_id, address_id, created_at, updated_at, created_by) VALUES
('Priya Sharma', '+91-9876543402', 'priya.sharma@email.com', 'https://example.com/patient2-photo.jpg', TRUE, 'CLAIMED', '234567890123', 'ABHA2345678901', @patient_address_2, NOW(), NOW(), 'system');

SET @global_patient_2 = LAST_INSERT_ID();

INSERT INTO md_global_patients (full_name, phone, email, profile_image_url, is_active, status, aadhar_id, abha_id, address_id, created_at, updated_at, created_by) VALUES
('Amit Patel', '+91-9876543403', 'amit.patel@email.com', 'https://example.com/patient3-photo.jpg', TRUE, 'UNCLAIMED', '345678901234', 'ABHA3456789012', @patient_address_3, NOW(), NOW(), 'system');

SET @global_patient_3 = LAST_INSERT_ID();

INSERT INTO md_global_patients (full_name, phone, email, profile_image_url, is_active, status, aadhar_id, abha_id, address_id, created_at, updated_at, created_by) VALUES
('Sneha Reddy', '+91-9876543404', 'sneha.reddy@email.com', 'https://example.com/patient4-photo.jpg', TRUE, 'CLAIMED', '456789012345', 'ABHA4567890123', @patient_address_4, NOW(), NOW(), 'system');

SET @global_patient_4 = LAST_INSERT_ID();

INSERT INTO md_global_patients (full_name, phone, email, profile_image_url, is_active, status, aadhar_id, abha_id, address_id, created_at, updated_at, created_by) VALUES
('Vikram Singh', '+91-9876543405', 'vikram.singh@email.com', 'https://example.com/patient5-photo.jpg', TRUE, 'CLAIMED', '567890123456', 'ABHA5678901234', @patient_address_5, NOW(), NOW(), 'system');

SET @global_patient_5 = LAST_INSERT_ID();

-- ============================================
-- 6. INSERT GLOBAL PATIENT EMERGENCY CONTACTS
-- ============================================
INSERT INTO md_emergency_contacts (global_patient_id, person_name, phone, relationship, email, is_primary, is_active, created_at, updated_at, created_by) VALUES
(@global_patient_1, 'Ramesh Kumar', '+91-9876543501', 'Father', 'ramesh.kumar@email.com', TRUE, TRUE, NOW(), NOW(), 'system'),
(@global_patient_1, 'Meera Kumar', '+91-9876543502', 'Mother', 'meera.kumar@email.com', FALSE, TRUE, NOW(), NOW(), 'system'),
(@global_patient_2, 'Rajesh Sharma', '+91-9876543503', 'Husband', 'rajesh.sharma@email.com', TRUE, TRUE, NOW(), NOW(), 'system'),
(@global_patient_3, 'Sunita Patel', '+91-9876543504', 'Wife', 'sunita.patel@email.com', TRUE, TRUE, NOW(), NOW(), 'system'),
(@global_patient_4, 'Kiran Reddy', '+91-9876543505', 'Brother', 'kiran.reddy@email.com', TRUE, TRUE, NOW(), NOW(), 'system'),
(@global_patient_5, 'Anita Singh', '+91-9876543506', 'Sister', 'anita.singh@email.com', TRUE, TRUE, NOW(), NOW(), 'system');

-- ============================================
-- 7. INSERT HOSPITAL PATIENTS
-- ============================================
INSERT INTO md_hospital_patients (hospital_id, global_patient_id, full_name, phone, email, is_active, notes, aadhar_id, abha_id, address_id, data_consent, created_at, updated_at, created_by) VALUES
(@hospital_1, @global_patient_1, 'Rajesh Kumar', '+91-9876543401', 'rajesh.kumar@email.com', TRUE, 'Regular patient, visits monthly for cardiac checkup', '123456789012', 'ABHA1234567890', @patient_address_1, TRUE, NOW(), NOW(), 'system');

SET @hospital_patient_1 = LAST_INSERT_ID();

INSERT INTO md_hospital_patients (hospital_id, global_patient_id, full_name, phone, email, is_active, notes, aadhar_id, abha_id, address_id, data_consent, created_at, updated_at, created_by) VALUES
(@hospital_1, @global_patient_2, 'Priya Sharma', '+91-9876543402', 'priya.sharma@email.com', TRUE, 'New patient, first visit', NULL, NULL, NULL, FALSE, NOW(), NOW(), 'system');

SET @hospital_patient_2 = LAST_INSERT_ID();

INSERT INTO md_hospital_patients (hospital_id, global_patient_id, full_name, phone, email, is_active, notes, aadhar_id, abha_id, address_id, data_consent, created_at, updated_at, created_by) VALUES
(@hospital_2, @global_patient_3, 'Amit Patel', '+91-9876543403', 'amit.patel@email.com', TRUE, 'Patient with chronic condition', '345678901234', 'ABHA3456789012', @patient_address_3, TRUE, NOW(), NOW(), 'system');

SET @hospital_patient_3 = LAST_INSERT_ID();

INSERT INTO md_hospital_patients (hospital_id, global_patient_id, full_name, phone, email, is_active, notes, aadhar_id, abha_id, address_id, data_consent, created_at, updated_at, created_by) VALUES
(@hospital_2, @global_patient_4, 'Sneha Reddy', '+91-9876543404', 'sneha.reddy@email.com', TRUE, 'Regular follow-up patient', '456789012345', 'ABHA4567890123', @patient_address_4, TRUE, NOW(), NOW(), 'system');

SET @hospital_patient_4 = LAST_INSERT_ID();

INSERT INTO md_hospital_patients (hospital_id, global_patient_id, full_name, phone, email, is_active, notes, aadhar_id, abha_id, address_id, data_consent, created_at, updated_at, created_by) VALUES
(@hospital_3, @global_patient_5, 'Vikram Singh', '+91-9876543405', 'vikram.singh@email.com', TRUE, 'Emergency patient, admitted last month', '567890123456', 'ABHA5678901234', @patient_address_5, TRUE, NOW(), NOW(), 'system');

SET @hospital_patient_5 = LAST_INSERT_ID();

INSERT INTO md_hospital_patients (hospital_id, global_patient_id, full_name, phone, email, is_active, notes, aadhar_id, abha_id, address_id, data_consent, created_at, updated_at, created_by) VALUES
(@hospital_3, @global_patient_1, 'Rajesh Kumar', '+91-9876543401', 'rajesh.kumar@email.com', TRUE, 'Patient transferred from Hospital 1', '123456789012', 'ABHA1234567890', @patient_address_1, TRUE, NOW(), NOW(), 'system');

SET @hospital_patient_6 = LAST_INSERT_ID();

-- ============================================
-- 8. INSERT HOSPITAL PATIENT EMERGENCY CONTACTS
-- ============================================
INSERT INTO md_hospital_emergency_contacts (hospital_patient_id, person_name, phone, relationship, email, is_primary, is_active, created_at, updated_at, created_by) VALUES
(@hospital_patient_1, 'Ramesh Kumar', '+91-9876543501', 'Father', 'ramesh.kumar@email.com', TRUE, TRUE, NOW(), NOW(), 'system'),
(@hospital_patient_1, 'Meera Kumar', '+91-9876543502', 'Mother', 'meera.kumar@email.com', FALSE, TRUE, NOW(), NOW(), 'system'),
(@hospital_patient_3, 'Sunita Patel', '+91-9876543504', 'Wife', 'sunita.patel@email.com', TRUE, TRUE, NOW(), NOW(), 'system'),
(@hospital_patient_4, 'Kiran Reddy', '+91-9876543505', 'Brother', 'kiran.reddy@email.com', TRUE, TRUE, NOW(), NOW(), 'system'),
(@hospital_patient_5, 'Anita Singh', '+91-9876543506', 'Sister', 'anita.singh@email.com', TRUE, TRUE, NOW(), NOW(), 'system'),
(@hospital_patient_6, 'Ramesh Kumar', '+91-9876543501', 'Father', 'ramesh.kumar@email.com', TRUE, TRUE, NOW(), NOW(), 'system');

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Uncomment to verify the inserted data:

-- SELECT * FROM md_addresses;
-- SELECT * FROM md_hospitals;
-- SELECT * FROM md_doctors;
-- SELECT * FROM md_global_patients;
-- SELECT * FROM md_hospital_patients;
-- SELECT * FROM md_emergency_contacts;
-- SELECT * FROM md_hospital_emergency_contacts;
-- SELECT * FROM md_awards;
-- SELECT * FROM md_social_media_links;
-- SELECT * FROM md_doctor_awards;
-- SELECT * FROM md_hospital_social_media;

-- ============================================
-- END OF SCRIPT
-- ============================================

